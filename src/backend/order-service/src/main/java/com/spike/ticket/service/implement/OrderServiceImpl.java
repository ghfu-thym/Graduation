package com.spike.ticket.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spike.ticket.client.TicketClient;
import com.spike.ticket.dto.TicketMetadata;
import com.spike.ticket.dto.event.CategoryItem;
import com.spike.ticket.dto.event.OrderConfirmedEvent;
import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.request.ReserveTicketRequest;
import com.spike.ticket.dto.respone.OrderResponse;
import com.spike.ticket.dto.respone.TicketReservationResponse;
import com.spike.ticket.entity.Order;
import com.spike.ticket.entity.OrderItem;
import com.spike.ticket.enums.OrderStatus;
import com.spike.ticket.kafka.publisher.OrderEventPublisher;
import com.spike.ticket.mapper.OrderMapper;
import com.spike.ticket.repository.OrderRepository;
import com.spike.ticket.service.OrderService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j // ghi log
public class OrderServiceImpl  implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final TicketClient ticketClient;
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {

        // tạo trước thông tin order, request giữ vé thành công sẽ lưu order xuống db
        Long totalAmount = 0L;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderTrackingNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);

        for (CategoryItem item : request.getCategoryItems()) {
            String redisKey = "ticket:category:" + item.getTicketCategoryId() + ":metadata";
            String jsonMetadata = redisTemplate.opsForValue().get(redisKey);

            if (jsonMetadata == null) {
                throw new RuntimeException("Ticket metadata not found for category ID: " + item.getTicketCategoryId());
            }

            TicketMetadata metadata;

            try {
                metadata = objectMapper.readValue(jsonMetadata, TicketMetadata.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Long price = metadata.getPrice()*(item.getQuantity());
            totalAmount += price;

            order.setEventId(metadata.getEventId());
            order.setEventName(metadata.getEventName());
            order.setBannerUrl(metadata.getBannerUrl());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setCategoryId(metadata.getCategoryId());
            orderItem.setPricePerTicket(metadata.getPrice());
            orderItem.setQuantity(item.getQuantity());

            orderItems.add(orderItem);


        }
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        //tạo request sang cho Inventory Service
        ReserveTicketRequest ticketRequest = new ReserveTicketRequest();
        ticketRequest.setEventID(request.getEventId());
        ticketRequest.setTicketCategories(request.getCategoryItems());


        TicketReservationResponse result = TicketReservationResponse.builder()
                .success(false)
                .build();
        try{
            result = ticketClient.reserveTicket(ticketRequest);
        } catch (FeignException.Conflict e){
            throw new RuntimeException("Failed in feign call to inventory service:");
        }

        if(!result.isSuccess()){
            Long index = result.getFailedCategoryIndex();
            OrderItem failedOrderItem = orderItems.get(index.intValue() - 1);
            String name = failedOrderItem.getName();
            throw new RuntimeException("Failed to reserve tickets: " + name);
        }

        Order savedOrder = orderRepository.save(order);

        String redisKey = "order_timeout:"+savedOrder.getOrderTrackingNumber();

//        redisTemplate.opsForValue().set(
//                redisKey,
//                "PENDING",
//                10,
//                TimeUnit.MINUTES);

        //Test
        redisTemplate.opsForValue().set(redisKey, "PENDING",10, TimeUnit.SECONDS);

        //TODO: message to payment service
        //

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderByTrackingNumber(String trackingNumber){
        Order order = orderRepository.findByOrderTrackingNumber(trackingNumber).orElseThrow(
                () ->new RuntimeException("Order not found for tracking number: "+trackingNumber)
        );
        return mapToResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(String orderTrackingNumber) {

        Order order = orderRepository.findByOrderTrackingNumber(orderTrackingNumber).orElseThrow(
                () ->new RuntimeException("Order not found for tracking number: "+orderTrackingNumber)
        );

        //
        if (order.getStatus() != OrderStatus.PENDING){
            throw new RuntimeException("Only can cancel pending order!");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Cancel order with ID: {}", orderTrackingNumber);

        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByUserID(Long userID, int page, int size ) {
        // Lưu ý cho FE: page -> số trang bắt đầu từ 0
        // size -> số phần tử 1 trang
        // sort theo id vì trong db id đang là auto incre
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Order> history = orderRepository.findByUserId(userID, pageable);
        return history.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public void completePayment(String orderTrackingNumber, String txnId) {

        Order order = orderRepository.findByOrderTrackingNumber(orderTrackingNumber).orElseThrow(
                () ->new RuntimeException("Order not found for tracking number: "+orderTrackingNumber)
        );

        if (order.getStatus() == OrderStatus.PAID){
            log.info("Order {} already paid, skipping.", orderTrackingNumber);
            return;
        }

        if (order.getStatus() == OrderStatus.CANCELLED|| order.getStatus() == OrderStatus.TIMEOUT){
            log.info("Order {} is cancelled or timeout, refund in process.", orderTrackingNumber);
            orderEventPublisher.publishRefundOrder(orderTrackingNumber, order.getTotalAmount());
            return;
        }


        order.setStatus(OrderStatus.PAID);
        order.setTxnId(txnId);
        orderRepository.save(order);

        // xóa redis
        redisTemplate.delete("order_timeout:" + orderTrackingNumber);

        // publish Kafka event để inventory service chuyển vé sang SOLD
        OrderConfirmedEvent event = mapToOrderConfirmedEvent(order);
        orderEventPublisher.publishOrderConfirmed(event);

        //TODO: message to notification service
    }


    // -------------------------------------------------------------------------
    // PRIVATE HELPER METHODS (MOCK)
    // -------------------------------------------------------------------------

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderConfirmedEvent mapToOrderConfirmedEvent(Order order) {
        List<OrderItem> orderItemList = order.getOrderItems();
        List<CategoryItem> categoryItems = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            CategoryItem tmp = new CategoryItem(
                    orderItem.getCategoryId(),
                    orderItem.getQuantity()
            );
            categoryItems.add(tmp);
        }
        return new OrderConfirmedEvent(
                order.getOrderTrackingNumber(),
                categoryItems
        );
    }
}

