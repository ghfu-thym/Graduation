package listener;

import com.spike.ticket.enums.OrderStatus;
import com.spike.ticket.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderTimeOutListener extends KeyExpirationEventMessageListener {

    private final OrderRepository orderRepository;
    public OrderTimeOutListener(RedisMessageListenerContainer listenerContainer, OrderRepository orderRepository) {
        super(listenerContainer);
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        try{
            String expireKey = message.toString();

            if(expireKey.startsWith("order_timeout:")){
                String orderID = expireKey.split(":")[1];
                Long id = Long.parseLong(orderID);

                log.info("Order with ID: {} is timed out!", id);

                processOrderTimeOut(id);
            }
        } catch (Exception e){
            log.error("Error processing order timeout event: {}", e.getMessage());
        }
    }

    private void processOrderTimeOut(Long orderId){
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING){
                order.setStatus(OrderStatus.TIMEOUT);
                orderRepository.save(order);

                log.info("Order with ID: {} is changed to timeout!", orderId);

                //TODO: Gửi message sang inventory service nhả ticket
            } else {
                log.info("Order {} cannot be changed to timeout! Current status:{}", orderId, order.getStatus());
            }
        });
    }
}
