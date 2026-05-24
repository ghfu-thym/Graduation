package com.spike.ticket.redis;

import com.spike.ticket.client.TicketClient;
import com.spike.ticket.dto.CategoryItem;
import com.spike.ticket.dto.OrderCancelledEvent;
import com.spike.ticket.entity.Order;
import com.spike.ticket.entity.OrderItem;
import com.spike.ticket.enums.OrderStatus;
import com.spike.ticket.kafka.publisher.OrderEventPublisher;
import com.spike.ticket.repository.OrderRepository;
import com.spike.ticket.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OrderTimeOutListener extends KeyExpirationEventMessageListener {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;
    public OrderTimeOutListener(RedisMessageListenerContainer listenerContainer, OrderRepository orderRepository, OrderService orderService, OrderEventPublisher orderEventPublisher) {
        super(listenerContainer);
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.orderEventPublisher = orderEventPublisher;
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

                orderService.cancelOrder(orderTrackingNumber);
                log.info("Order with tracking number: {} is changed to cancelled!", orderTrackingNumber);

            } else {
                log.info("Order {} cannot be changed to timeout! Current status:{}", orderTrackingNumber, order.getStatus());
            }
        });
    }


}
