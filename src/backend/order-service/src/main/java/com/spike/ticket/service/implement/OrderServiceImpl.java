package com.spike.ticket.service.implement;

import com.spike.ticket.client.TicketClient;
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
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Create order request for user: {}", request.getUserId());

        ReserveTicketRequest ticketRequest = new ReserveTicketRequest();
        ticketRequest.setEventID(request.getEventId());
        ticketRequest.setTicketIds(request.getTicketIds());
        ticketRequest.setTicketType(request.getTicketType());
        ticketRequest.setQuantity(request.getQuantity());


        List<TicketReservationResponse> reservedTickets;
        try {
            reservedTickets = ticketClient.reserveTicket(ticketRequest);
        } catch (FeignException.Conflict e){
            throw new RuntimeException("Failed to reserve tickets:");
        }


        Order order = new Order();
        order.setOrderTrackingNumber(UUID.randomUUID().toString());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(request.getTotalPrice());


        Long totalAmount = 0L;
        List<OrderItem> orderItems = new ArrayList<>();


        for(TicketReservationResponse ticket : reservedTickets){
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .ticketId(ticket.getTicketId())
                    .price(ticket.getPrice())
                    .build();
            orderItems.add(item);
            totalAmount += ticket.getPrice();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with tracking number: {}", savedOrder.getOrderTrackingNumber());

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

        if (order.getStatus() != OrderStatus.PENDING){
            throw new RuntimeException("Order is unavailable to be paid!");
        }

        order.setStatus(OrderStatus.PAID);
        order.setTxnId(txnId);
        orderRepository.save(order);

        // xóa redis
        redisTemplate.delete("order_timeout:" + orderTrackingNumber);

        // publish Kafka event để inventory service chuyển vé sang SOLD
        List<Long> ticketIds = order.getOrderItems().stream()
                .map(OrderItem::getTicketId)
                .toList();
        orderEventPublisher.publishOrderConfirmed(orderTrackingNumber, ticketIds);

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
    /**
     * Giả lập việc gọi sang Inventory Service để kiểm tra ghế.
     * Logic:
     * 1. Tạm dừng 50-100ms để mô phỏng Network Latency (Quan trọng cho High Concurrency test).
     * 2. Nếu trong list có ghế ID = 999 (ví dụ) -> Trả về False (để test case thất bại).
     * 3. Còn lại trả về True.
     */
    private boolean mockCheckSeatAvailability(java.util.List<Long> seatIds) {
        log.info("Mocking request to Inventory Service to check seats: {}", seatIds);

        try {
            // Giả lập độ trễ mạng (Network Latency)
            // Trong thực tế, gọi sang service khác mất tầm 30ms - 200ms
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while mocking network call", e);
        }

        // GIẢ LẬP LỖI (Để bạn test trường hợp mua thất bại)
        // Quy ước: Nếu user chọn ghế số 999 hoặc số âm -> Báo là ghế đã hết
        for (Long id : seatIds) {
            if (id < 0 || id == 999) {
                log.warn("Mock: Seat ID {} is already taken or invalid!", id);
                return false; // Ghế không khả dụng
            }
        }

        // Mặc định là ghế trống
        return true;
    }
}

