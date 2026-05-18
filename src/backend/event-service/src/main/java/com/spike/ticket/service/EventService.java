package com.spike.ticket.service;

import com.spike.ticket.dto.CreateEventRequest;
import com.spike.ticket.dto.CreateTicketRequest;
import com.spike.ticket.dto.EventResponse;
import com.spike.ticket.dto.PresignedUrlResponse;
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

    @Transactional
    public EventResponse createEvent(Long creatorId,String username, String email, CreateEventRequest request) {
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
                .status(EventStatus.DRAFT)
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

        // thêm ORGANIZER
        EventMember eventMember = EventMember.builder()
                .eventId(savedEvent.getEventId())
                .userId(creatorId)
                .role(EventRole.ORGANIZER)
                .build();
        eventMemberRepository.save(eventMember);

        log.info("Created event '{}' with {} images by user {}",
                savedEvent.getName(), imageUrls.size(), creatorId);

        return EventResponse.fromEntity(savedEvent, imageUrls);
    }

    public PresignedUrlResponse getUploadPermission(String fileName, String contentType) {
        return s3Service.generatePreSignedUrl(fileName, contentType);
    }

    @Transactional
    public void addInspector(Long eventId, List<Long> userIds) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new IllegalArgumentException("Event not found")
        );
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new RuntimeException("Event is not in draft status");
        }
        for (Long userId : userIds) {
            EventMember eventMember = EventMember.builder()
                    .eventId(eventId)
                    .userId(userId)
                    .role(EventRole.INSPECTOR)
                    .build();
            eventMemberRepository.save(eventMember);
        }
    }

    @Transactional
    public void syncTicketCategory(Long eventId, CreateTicketRequest request){

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
        List<Long> memberList = eventMemberList.stream().map(EventMember::getUserId).toList();

        event.setStatus(EventStatus.PUBLISHED);

        eventServicePublisher.publishEventApprovedInventory(eventId,ticketCategories, event.getName(), event.getOrganizerId(), event.getOrganizerName(), event.getOrganizerEmail());
        eventServicePublisher.publishEventApprovedMember(eventId, memberList);

        //TODO: do something that user can see event on web

        eventRepository.save(event);
    }
}
