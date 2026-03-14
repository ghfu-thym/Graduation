package com.spike.ticket.service;


import com.spike.ticket.dto.*;
import com.spike.ticket.dto.event.EventApprovedMessage;
import com.spike.ticket.dto.event.TicketCategoryDTO;
import com.spike.ticket.entity.InventoryTicket;
import com.spike.ticket.entity.ProcessedOrder;
import com.spike.ticket.entity.TicketCategory;
import com.spike.ticket.repository.InventoryTicketRepo;
import com.spike.ticket.repository.ProcessedOrderRepository;
import com.spike.ticket.repository.TicketCategoryRepo;
import com.spike.ticket.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketCategoryRepo ticketCategoryRepo;
    private final InventoryTicketRepo inventoryTicketRepo;
    private final ProcessedOrderRepository processedOrderRepo;
    private final StringRedisTemplate redisTemplate;
    private DefaultRedisScript<Long> reserveScript;
    private DefaultRedisScript<Long> releaseScript;
    private DefaultRedisScript<Long> initScript;

    // load file lua script
    @PostConstruct
    public void init() {
        reserveScript = new DefaultRedisScript<>();
        reserveScript.setLocation(new ClassPathResource("scripts/reserve_ticket.lua"));
        reserveScript.setResultType(Long.class);

        releaseScript = new DefaultRedisScript<>();
        releaseScript.setLocation(new ClassPathResource("scripts/release_ticket.lua"));
        releaseScript.setResultType(Long.class);

        initScript = new DefaultRedisScript<>();
        initScript.setLocation(new ClassPathResource("scripts/init_ticket.lua"));
        initScript.setResultType(Long.class);
    }

    public TicketReservationResponse reserveTicket(ReserveTicketRequest request){


        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        List<CategoryItem> categoryItemList = request.getCategoryItemList();

        // 1. Chuyển đổi List input thành mảng KEYS và ARGV cho Redis
        for (CategoryItem item : categoryItemList) {
            // VD: ticket:category:1:stock
            keys.add("ticket:category:" + item.getTicketCategoryId().toString() + ":stock");
            // Số lượng cần trừ
            args.add(String.valueOf(item.getQuantity()));
        }

        // 2. Thực thi Lua Script
        // Redis sẽ đảm bảo khóa luồng, không ai chen ngang được vào thao tác này
        Long result = redisTemplate.execute(reserveScript, keys, args.toArray());


        // 3. Xử lý kết quả
        if (result != null && result == 0L) {
            return new TicketReservationResponse(true, null);

        } else {
            return new TicketReservationResponse(false, result);
        }
    }

    public void releaseTickets(ReleaseTicketRequest request) {
        if(request.getCategoryItems() == null || request.getCategoryItems().isEmpty()){
            return;
        }

        if(processedOrderRepo.existsByOrderTrackingNumber(request.getOrderTrackingNumber())){
            return;
        }

        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        List<CategoryItem> categoryItemList = request.getCategoryItems();

        for (CategoryItem item : categoryItemList) {
            keys.add("ticket:category:" + item.getTicketCategoryId().toString() + "stock");
            args.add(String.valueOf(item.getQuantity()));
        }


        Long result = redisTemplate.execute(initScript, keys, args.toArray());

        if (result == 1L) {
            log.info("Released ticket");

        } else {
            log.warn("Failed to release ticket");

        }

    }

    @Transactional
    public void confirmTickets(ConfirmTicketRequest request) {
        if(request.getCategoryItemList() == null || request.getCategoryItemList().isEmpty()){
            return;
        }
        // kiểm tra xem order đã được xử lý trước đó chưa
        if (processedOrderRepo.existsByOrderTrackingNumber(request.getOrderTrackingNumber())){
            return;
        }
        //int confirmCount = ticketRepository.confirmTickets(request.getTicketIds());
        //log.info("Confirmed {} tickets, order tracking number: {}", confirmCount, request.getOrderTrackingNumber());

        // trừ kho
        for (CategoryItem categoryItem : request.getCategoryItemList()){
            InventoryTicket inventoryTicket = inventoryTicketRepo.findById(categoryItem.getTicketCategoryId()).orElseThrow(
                    () -> new RuntimeException("Inventory ticket ID: " + categoryItem.getTicketCategoryId() + " not found!")
            );

            inventoryTicket.setAvailableQuantity(inventoryTicket.getAvailableQuantity() - categoryItem.getQuantity());
            inventoryTicketRepo.save(inventoryTicket);


        }

        // lưu order đã xử lý để tránh trùng lặp khi consumer nhận lại message
        ProcessedOrder processedOrder = ProcessedOrder.builder()
                .orderTrackingNumber(request.getOrderTrackingNumber())
                .build();
        processedOrderRepo.save(processedOrder);
    }

    @Transactional
    public void handleEventApproved (EventApprovedMessage message) {
        for (TicketCategoryDTO ticketCategoryDTO: message.ticketCategories()){
            // lưu vào TicketCategory
            TicketCategory ticketCategory = TicketCategory.builder()
                    .eventId(message.eventId())
                    .name(ticketCategoryDTO.name())
                    .price(ticketCategoryDTO.price())
                    .quantity(ticketCategoryDTO.quantity())
                    .build();
            TicketCategory saved = ticketCategoryRepo.save(ticketCategory);

            // sinh số lượng vé lưu vào InventoryTicket, redis lấy từ đây tạo vé
            InventoryTicket inventoryTicket = InventoryTicket.builder()
                    .ticketCategoryId(saved.getId())
                    .name(saved.getName())
                    .totalQuantity(saved.getQuantity())
                    .availableQuantity(saved.getQuantity())
                    .price(saved.getPrice())
                    .build();
            inventoryTicketRepo.save(inventoryTicket);
        }
    }


    public void initTicket(Long eventId){
        List<TicketCategory> ticketCategories = ticketCategoryRepo.findByEventId(eventId);

        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        for (TicketCategory ticketCategory : ticketCategories) {
            keys.add("ticket:category:" + ticketCategory.getId().toString() + "stock");
            args.add(String.valueOf(ticketCategory.getQuantity()));
        }

        Long result = redisTemplate.execute(initScript, keys, args.toArray());

        if (result >0){
            log.info("Init {} ticket categories success for event: {}", result, eventId);
        } else {
            log.info("Categories already init for event: {}" , eventId);
        }
    }
}
