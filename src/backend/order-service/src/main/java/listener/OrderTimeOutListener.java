package listener;

import com.spike.ticket.client.TicketClient;
import com.spike.ticket.dto.request.ReleaseTicketRequest;
import com.spike.ticket.entity.OrderItem;
import com.spike.ticket.enums.OrderStatus;
import com.spike.ticket.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrderTimeOutListener extends KeyExpirationEventMessageListener {

    private final OrderRepository orderRepository;
    private final TicketClient ticketClient;
    public OrderTimeOutListener(RedisMessageListenerContainer listenerContainer, OrderRepository orderRepository, TicketClient ticketClient) {
        super(listenerContainer);
        this.orderRepository = orderRepository;
        this.ticketClient = ticketClient;
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        try{
            String expireKey = message.toString();

            if(expireKey.startsWith("order_timeout:")){
                String orderTrackingNumber = expireKey.replace("order_timeout:", "");

                log.info("Order with tracking number: {} is timed out!", orderTrackingNumber);

                processOrderTimeOut(orderTrackingNumber);
            }
        } catch (Exception e){
            log.error("Error processing order timeout event: {}", e.getMessage());
        }
    }

    private void processOrderTimeOut(String orderTrackingNumber){
        orderRepository.findByOrderTrackingNumber(orderTrackingNumber).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING){
                order.setStatus(OrderStatus.TIMEOUT);
                orderRepository.save(order);

                log.info("Order with tracking number: {} is changed to timeout!", orderTrackingNumber);


                List<Long> ticketIds = order.getOrderItems().stream()
                        .map(OrderItem::getTicketId)
                        .toList();
                if (!ticketIds.isEmpty()){
                    try{
                        ReleaseTicketRequest request = ReleaseTicketRequest.builder()
                                .ticketIds(ticketIds)
                                .orderTrackingNumber(orderTrackingNumber)
                                .build();
                        ticketClient.releaseTicket(request);
                    } catch (Exception e){
                        log.error("Error releasing tickets for orderID {}, message: {}",orderTrackingNumber, e.getMessage());
                    }
                }
            } else {
                log.info("Order {} cannot be changed to timeout! Current status:{}", orderTrackingNumber, order.getStatus());
            }
        });
    }
}
