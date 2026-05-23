package com.spike.ticket.service;

import com.spike.ticket.dto.*;
import com.spike.ticket.entity.Event;
import com.spike.ticket.entity.EventImage;
import com.spike.ticket.entity.EventMember;
import com.spike.ticket.entity.TicketCategory;
import com.spike.ticket.enums.EventRole;
import com.spike.ticket.enums.EventStatus;
import com.spike.ticket.kafka.EventServicePublisher;
import com.spike.ticket.repository.EventImageRepository;
import com.spike.ticket.repository.EventMemberRepository;
import com.spike.ticket.repository.EventRepository;
import com.spike.ticket.repository.TicketCategoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final EventImageRepository eventImageRepository;
    private final EventServicePublisher eventServicePublisher;
    private final TicketCategoryRepo ticketCategoryRepo;
    private final S3Service s3Service;
    private final DynamoService dynamoService;

    private final int SHARD_SIZE = 50;


    public EventDetail getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new IllegalArgumentException("Event not found")
        );

        List<EventImage> orderedImages = eventImageRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
        List<String> imageUrls = orderedImages.isEmpty()
                ? (event.getImageUrls() == null ? List.of() : event.getImageUrls())
                : orderedImages.stream().map(EventImage::getImageUrl).toList();

        List<TicketCategory> ticketCategories = ticketCategoryRepo.findByEventId(eventId);

        return EventDetail.builder()
                .id(event.getEventId())
                .name(event.getName())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .description(event.getDescription())
                .imageUrls(imageUrls)
                .categoryItemList(ticketCategories)
                .shardCount(event.getShardCount())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public List<EventSummarize> getHomeData() {
        List<Event> eventList = eventRepository.findEventByStatus(EventStatus.PUBLISHED);
        return getEventSummarizes(eventList);
    }

    public List<EventSummarize> getDraftEvent() {
        List<Event> eventList = eventRepository.findEventByStatus(EventStatus.DRAFT);
        return getEventSummarizes(eventList);
    }

    private List<EventSummarize> getEventSummarizes(List<Event> eventList) {
        List<EventSummarize> summarizeList = new ArrayList<>();
        for (Event event : eventList) {
            EventSummarize summarize = EventSummarize.builder()
                    .eventId(event.getEventId())
                    .eventName(event.getName())
                    .startTime(event.getStartTime().toString())
                    .location(event.getLocation())
                    .minPrice(ticketCategoryRepo.findMinPriceByEventId(event.getEventId()))
                    .imageUrl(event.getImageUrls().isEmpty() ? "" : event.getImageUrls().get(0))
                    .build();
            summarizeList.add(summarize);
        }

        return summarizeList;
    }

    // tan dung entity
    public List<TicketCategory> getTicketCategoryByEventId(Long eventId) {
        return ticketCategoryRepo.findByEventId(eventId);
    }

    @Transactional
    public EventResponse createEvent(Long creatorId, String username, String email, CreateEventRequest request) {
        // Validate: endTime phải sau startTime
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        //
        Event newEvent = Event.builder()
                .name(request.getName())
                .organizerId(creatorId)
                .organizerName(username)
                .organizerEmail(email)
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .ticketOpenTime(request.getTicketOpenTime())
                .status(EventStatus.DRAFT)
                .isOpened(false)
                .description(request.getDescription())
                .imageUrls(request.getListOfImageUrls())
                .build();
        Event savedEvent = eventRepository.save(newEvent);

        // lưu ảnh
        List<String> imageUrls = newEvent.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<EventImage> eventImages = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                EventImage img = EventImage.builder()
                        .event(savedEvent)
                        .imageUrl(imageUrls.get(i))
                        .displayOrder(i)
                        .build();
                eventImages.add(img);
            }
            if (!eventImages.isEmpty()) {
                eventImageRepository.saveAll(eventImages);
            }
        }

        List<CreateCategoryRequest> requestList = request.getTicketCategoryList();
        int totalGuest = 0;
        for (CreateCategoryRequest categoryRequest : requestList) {
            TicketCategory category = TicketCategory.builder()
                    .eventId(savedEvent.getEventId())
                    .name(categoryRequest.getName())
                    .price(categoryRequest.getPrice())
                    .quantity(categoryRequest.getQuantity())
                    .description(categoryRequest.getDescription())
                    .build();
            totalGuest += categoryRequest.getQuantity();
            ticketCategoryRepo.save(category);
        }

        savedEvent.setShardCount(totalGuest/SHARD_SIZE + 1);

        // thêm ORGANIZER
        EventMember eventMember = EventMember.builder()
                .eventId(savedEvent.getEventId())
                .email(email)
                .role(EventRole.ORGANIZER)
                .build();
        eventMemberRepository.save(eventMember);

        // them inspector
        addInspector(savedEvent.getEventId(), request.getMemberEmailList());

        log.info("Created event '{}' with {} images by user {}",
                savedEvent.getName(), imageUrls.size(), creatorId);

        return EventResponse.fromEntity(savedEvent, imageUrls);
    }

    public PresignedUrlResponse getUploadPermission(String fileName, String contentType) {
        return s3Service.generatePreSignedUrl(fileName, contentType);
    }

    @Transactional
    public void addInspector(Long eventId, List<String> userEmails) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new IllegalArgumentException("Event not found")
        );
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new RuntimeException("Event is not in draft status");
        }
        for (String userEmail : userEmails) {
            EventMember eventMember = EventMember.builder()
                    .eventId(eventId)
                    .email(userEmail)
                    .role(EventRole.INSPECTOR)
                    .build();
            eventMemberRepository.save(eventMember);
        }
    }

    @Transactional
    public void syncTicketCategory(Long eventId, CreateTicketRequest request) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new IllegalArgumentException("Event not found")
        );

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalArgumentException("Event is not in draft status");
        }

        ticketCategoryRepo.deleteByEventId(eventId);

        ticketCategoryRepo.saveAll(request.getTicketCategories());

    }

    @Transactional // nếu kafka fail thì roll back
    public void publishEvent(Long eventId) {
        Event event = eventRepository.findByEventId(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        if (event.getStatus() != EventStatus.DRAFT) {
            return;
        }


        List<TicketCategory> ticketCategories = ticketCategoryRepo.findByEventId(eventId);
        if (ticketCategories.isEmpty()) {
            throw new IllegalArgumentException("Event has no ticket categories");
        }
        List<EventMember> eventMemberList = eventMemberRepository.findByEventId(eventId);
        if (eventMemberList.isEmpty()) {
            throw new IllegalArgumentException("Event has no members");
        }
        List<String> memberEmailList = eventMemberList.stream().map(EventMember::getEmail).toList();

        event.setStatus(EventStatus.PUBLISHED);

        eventServicePublisher.publishEventApprovedInventory(eventId, ticketCategories, event.getName(), event.getOrganizerId(), event.getOrganizerName(), event.getOrganizerEmail());
        eventServicePublisher.publishEventApprovedMember(eventId, memberEmailList);

        // init config on VWR
        dynamoService.updateEventStatus(eventId.toString(), "NORMAL", event.getShardCount());

        eventRepository.save(event);
    }
}
