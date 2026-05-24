package com.spike.ticket.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spike.ticket.client.PaymentClient;
import com.spike.ticket.client.TicketClient;
import com.spike.ticket.dto.*;
import com.spike.ticket.dto.request.CreateOrderRequest;
import com.spike.ticket.dto.request.PaymentRequest;
import com.spike.ticket.dto.request.ReserveTicketRequest;
import com.spike.ticket.dto.respone.OrderResponse;
import com.spike.ticket.dto.respone.TicketReservationResponse;
import com.spike.ticket.entity.Order;
import com.spike.ticket.entity.OrderItem;
import com.spike.ticket.entity.Ticket;
import com.spike.ticket.enums.OrderStatus;
import com.spike.ticket.kafka.publisher.OrderEventPublisher;
import com.spike.ticket.mapper.OrderMapper;
import com.spike.ticket.repository.OrderRepository;
import com.spike.ticket.service.DynamoService;
import com.spike.ticket.service.OrderService;
import com.spike.ticket.utils.HmacUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j // ghi log
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final TicketClient ticketClient;
    private final PaymentClient paymentClient;
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectMapper objectMapper;
    private final DynamoService dynamoService;

    // giới hạn bật VWR
    private final int THRESHOLD = 10;

    @Value("${app.ticket.hmac-secret}")
    private String secretKey;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId, String email, String username) {

        //idempotency, check xem user này đã tạo order trước đó chưa
        if(orderRepository.existsByUserIdAndOrderStatus(userId, OrderStatus.PENDING) > 0) {
            throw new RuntimeException("You have a pending order, please complete or cancel it before creating a new one.");
        }

        // tạo trước thông tin order, request giữ vé thành công sẽ lưu order xuống db
        Long totalAmount = 0L;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setUserId(userId);
        order.setUserName(username);
        order.setUserEmail(email);
        order.setOrderTrackingNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);

        for (CategoryItem item : request.getCategoryItems()) {
            String redisKey = "ticket:category:" + item.getTicketCategoryId() + ":metadata";
            String jsonMetadata = redisTemplate.opsForValue().get(redisKey);

            if (jsonMetadata == null) {
                log.error("Ticket metadata not found for category ID: {}", request);
                throw new RuntimeException("Ticket metadata not found for category ID: " + item.getTicketCategoryId());
            }

            TicketMetadata metadata;

            try {
                metadata = objectMapper.readValue(jsonMetadata, TicketMetadata.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Long price = metadata.getPrice() * (item.getQuantity());
            totalAmount += price;

            order.setEventId(metadata.getEventId());
            order.setEventName(metadata.getEventName());
            order.setBannerUrl(metadata.getBannerUrl());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setCategoryId(metadata.getCategoryId());
            orderItem.setPricePerTicket(metadata.getPrice());
            orderItem.setName(metadata.getCategoryName());
            orderItem.setQuantity(item.getQuantity());

            orderItems.add(orderItem);


        }
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        //tạo request sang cho Inventory Service
        ReserveTicketRequest ticketRequest = new ReserveTicketRequest();
        ticketRequest.setEventID(request.getEventId());
        ticketRequest.setCategoryItemList(request.getCategoryItems());


        TicketReservationResponse result = TicketReservationResponse.builder()
                .success(false)
                .build();
        try {
            result = ticketClient.reserveTicket(ticketRequest);
        } catch (Exception e) {

            log.error("Failed in feign call to inventory service:", e);
        }

        if (!result.isSuccess()) {
            Long index = result.getFailedCategoryIndex();
            OrderItem failedOrderItem = orderItems.get(index.intValue() - 1);
            String name = failedOrderItem.getName();
            throw new RuntimeException("Failed to reserve tickets: " + name);
        }

        Order savedOrder = orderRepository.save(order);

        String redisKey = "order_timeout:" + savedOrder.getOrderTrackingNumber();

//        redisTemplate.opsForValue().set(
//                redisKey,
//                "PENDING",
//                10,
//                TimeUnit.MINUTES);

        //Test
        redisTemplate.opsForValue().set(redisKey, "PENDING", 3, TimeUnit.MINUTES);

        // bộ đếm số đơn PENDING
        // nếu chưa có thì tự tạo
        Long currentCount = redisTemplate.opsForValue().increment("event:counter:"+order.getEventId());
        log.info("Current count increased 1 for event {}, is {}", order.getEventId(), currentCount);
        if (currentCount != null && currentCount == (THRESHOLD+1)) {

            dynamoService.updateEventStatus(order.getEventId().toString(),"QUEUING");
            log.info("Turn on VWR for event: {}", order.getEventId());
        }


        //request to payment service
        PaymentRequest paymentRequest = new PaymentRequest(savedOrder.getOrderTrackingNumber(), savedOrder.getTotalAmount());
        ResponseEntity<String> paymentResponse = paymentClient.createPaymentUrl(paymentRequest);
        String paymentUrl = paymentResponse.getBody();

        if (paymentUrl == null || paymentUrl.isBlank()) {
            throw new RuntimeException("Payment URL is empty");
        }

        //String paymentUrl = "https://www.google.com";

        return mapToResponse(savedOrder, paymentUrl);
    }

    public OrderResponse getOrderByTrackingNumber(String trackingNumber) {
        Order order = orderRepository.findByOrderTrackingNumber(trackingNumber).orElseThrow(
                () -> new RuntimeException("Order not found for tracking number: " + trackingNumber)
        );
        return mapToResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(String orderTrackingNumber) {

        Order order = orderRepository.findByOrderTrackingNumber(orderTrackingNumber).orElseThrow(
                () -> new RuntimeException("Order not found for tracking number: " + orderTrackingNumber)
        );



        decreaseCounter(order);
        log.info("cancel call decreaseCounter");

        try{
            orderEventPublisher.publishOrderCancelled(mapToOrderCancelledEvent(order));
            //ticketClient.releaseTicket(request);
        } catch (Exception e){
            log.error("Error releasing tickets for orderID {}, message: {}",orderTrackingNumber, e.getMessage());
        }
        log.info("Cancel order with ID: {}", orderTrackingNumber);

        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByUserID(Long userID, int page, int size) {
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
                () -> new RuntimeException("Order not found for tracking number: " + orderTrackingNumber)
        );

        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Order {} already paid, skipping.", orderTrackingNumber);
            return;
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.TIMEOUT) {
            log.info("Order {} is cancelled or timeout, refund in process.", orderTrackingNumber);
            orderEventPublisher.publishRefundOrder(orderTrackingNumber, order.getTotalAmount());
            return;
        }


        order.setStatus(OrderStatus.PAID);
        order.setTxnId(txnId);


        // xóa redis
        redisTemplate.delete("order_timeout:" + orderTrackingNumber);

        //giảm counter

        decreaseCounter(order);
        log.info("completePayment call decreaseCounter");

        // publish Kafka event để inventory service lưu cứng vào db
        OrderConfirmedEvent event = mapToOrderConfirmedEvent(order);
        orderEventPublisher.publishOrderConfirmed(event);

        //list ticket gắn trong order, sau này tra lịch sử mua sẽ ra
        List<Ticket> ticketList = new ArrayList<>();
        List<Ticket> ticketListInDB = order.getTicketList();
        ticketListInDB.clear();


        //list ticket bắn event
        List<TicketDetail> ticketDetails = new ArrayList<>();

        //tạo vé
        for (OrderItem orderItem : order.getOrderItems()) {
            for (int i = 0; i < orderItem.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                String ticketNumber = generateTicketNumber(order.getEventId(), orderItem.getCategoryId());
                ticket.setTicketNumber(ticketNumber);
                ticket.setOrder(order);
                ticket.setTicketCategoryId(orderItem.getCategoryId());
                ticket.setCategoryName(orderItem.getName());

                // sinh mã qr
                // Ví dụ: eventID|categoryId|ticketNumber.signature
                String payload = order.getEventId() + "|" + orderItem.getCategoryId() + "|" + ticketNumber;
                String signature = HmacUtils.signHmacSha256(payload, secretKey);
                String qrCode = payload + "." + signature;
                ticket.setQrCode(qrCode);

                TicketDetail ticketDetail = new TicketDetail(
                        ticketNumber,
                        orderItem.getCategoryId(),
                        orderItem.getName(),
                        qrCode
                );


                ticketList.add(ticket);
                ticketDetails.add(ticketDetail);
            }
        }

        for (Ticket ticket : ticketList) {
            ticket.setOrder(order);
            ticketListInDB.add(ticket);
        }


        orderRepository.save(order);


        // kafka event cho notification service gửi email, checkin service lấy data vé
        orderEventPublisher.ticketCreated(new TicketCreatedEvent(
                order.getEventId(),
                order.getOrderTrackingNumber(),
                order.getEventName(),
                order.getUserName(),
                order.getUserEmail(),
                ticketDetails
        ));
    }

    private void decreaseCounter(Order order) {
        String redisKey = "event:counter:" + order.getEventId();
        Long currentCount = redisTemplate.opsForValue().decrement(redisKey);
        log.info("Current count decreased 1 for event {}, is {}", order.getEventId(), currentCount);

        if (currentCount != null && currentCount == THRESHOLD) {
            dynamoService.updateEventStatus(order.getEventId().toString(),"NORMAL");
            log.info("Turn off VWR for event: {}", order.getEventId());
        }

        // counter tự xóa sau 1h
        if (currentCount != null && currentCount == 0) {
            redisTemplate.expire("event:counter:"+order.getEventId(), 1, TimeUnit.HOURS);
        }
    }

    private OrderCancelledEvent mapToOrderCancelledEvent(Order order) {
        Order orderWithItems = orderRepository.findByTrackingWithItems(order.getOrderTrackingNumber())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + order.getOrderTrackingNumber()));

        List<OrderItem> orderItemList = orderWithItems.getOrderItems();
        List<CategoryItem> categoryItems = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            CategoryItem tmp = new CategoryItem(
                    orderItem.getCategoryId(),
                    orderItem.getQuantity()
            );
            categoryItems.add(tmp);
        }
        return new OrderCancelledEvent(
                order.getOrderTrackingNumber(),
                categoryItems
        );
    }

    private OrderResponse mapToResponse(Order order) {
        return mapToResponse(order, null);
    }

    private OrderResponse mapToResponse(Order order, String paymentUrl) {
        return OrderResponse.builder()
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .paymentUrl(paymentUrl)
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

    //Định dạng: [EVENT_ID]-[CATEGORY_ID]-[TIMESTAMP_MILLIS]-[RANDOM]
    private String generateTicketNumber(Long eventId, Long categoryId) {

        long timestampMillis = System.currentTimeMillis();
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return String.format("%d-%d-%d-%s", eventId, categoryId, timestampMillis, random);
    }
}

