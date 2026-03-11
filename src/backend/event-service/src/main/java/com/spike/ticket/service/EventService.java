package com.spike.ticket.service;

import com.spike.ticket.dto.CreateEventRequest;
import com.spike.ticket.dto.EventResponse;
import com.spike.ticket.entity.Event;
import com.spike.ticket.entity.EventImage;
import com.spike.ticket.entity.EventMember;
import com.spike.ticket.enums.EventRole;
import com.spike.ticket.enums.EventStatus;
import com.spike.ticket.repository.EventImageRepository;
import com.spike.ticket.repository.EventMemberRepository;
import com.spike.ticket.repository.EventRepository;
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
    private final FileStorageService fileStorageService;

    @Transactional
    public EventResponse createEvent(Long creatorId, CreateEventRequest request) {
        // Validate: endTime phải sau startTime
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        //
        Event newEvent = Event.builder()
                .name(request.getName())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(EventStatus.DRAFT)
                .description(request.getDescription())
                .build();
        Event savedEvent = eventRepository.save(newEvent);

        // lưu ảnh
        List<String> imageUrls = fileStorageService.storeImages(request.getImages());
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

        // thêm ORGANIZER
        EventMember eventMember = EventMember.builder()
                .eventId(savedEvent.getId())
                .userId(creatorId)
                .role(EventRole.ORGANIZER)
                .build();
        eventMemberRepository.save(eventMember);

        log.info("Created event '{}' with {} images by user {}",
                savedEvent.getName(), imageUrls.size(), creatorId);

        return EventResponse.fromEntity(savedEvent, imageUrls);
    }

    @Transactional
    public void addInspector(Long eventId, List<Long> userIds) {
        for (Long userId : userIds) {
            EventMember eventMember = EventMember.builder()
                    .eventId(eventId)
                    .userId(userId)
                    .role(EventRole.INSPECTOR)
                    .build();
            eventMemberRepository.save(eventMember);
        }
    }

    public void publishEvent(Long eventId) {
        Event event = eventRepository.findByEventId(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        if (event.getStatus() != EventStatus.DRAFT) {
            return;
        }

        event.setStatus(EventStatus.PUBLISHED);

        //TODO: do something that user can see event on web


        eventRepository.save(event);
    }
}
