package com.spike.ticket.controller;

import com.spike.ticket.dto.*;
import com.spike.ticket.entity.TicketCategory;
import com.spike.ticket.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping("/{eventId}/info-detail")
    public ResponseEntity<EventDetail> getEventById(@PathVariable Long eventId) {
        log.info("Get event by id: {}", eventId);
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(eventService.getEventById(eventId));

        } catch (IllegalArgumentException e) {
            log.error("Event not found: {}", eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error fetching event by id: {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{eventId}/test")
    public ResponseEntity<String> test(@PathVariable Long eventId) {
        return ResponseEntity.ok("Test API for eventId: " + eventId);
    }

    @GetMapping("/{eventId}/ticket-categories")
    public ResponseEntity<List<TicketCategory>> getTicketCategoryByEventId(@PathVariable Long eventId){
        log.info("Get ticket category by event id: {}", eventId);
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(eventService.getTicketCategoryByEventId(eventId));
        } catch (IllegalArgumentException e) {
            log.error("Event not found: {}", eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error fetching ticket categories for event id: {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/info-home")
    public ResponseEntity<List<EventSummarize>> getHomeData(){
        log.info("Get home data");
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                    .body(eventService.getHomeData());
        } catch (Exception e) {
            log.error("Error fetching home data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/draft-event")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<EventSummarize>> getAllEvent(){
        log.info("ADMIN get all event");
        try{
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS))
                    .body(eventService.getDraftEvent());
        } catch (Exception e){
            log.error("Error fetching all event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping(value = "/create")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            JwtAuthenticationToken auth) {
        log.info("Create event request: {}", request);

        Long userId = Long.parseLong(auth.getToken().getSubject());
        String username = auth.getToken().getClaimAsString("username");
        String email = auth.getToken().getClaimAsString("email");
        try {
            EventResponse response = eventService.createEvent(userId, username, email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating event: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/upload-url")
    public ResponseEntity<?> generatePreSignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType) {
        log.info("Generate pre-signed URL for file: {} with content type: {}", fileName, contentType);
        return ResponseEntity.ok(eventService.getUploadPermission(fileName, contentType));
    }

    @PreAuthorize("@eventAuth.isOrganizer(#eventId, authentication)")
    @PutMapping("/{eventId}/update")
    public String updateEvent(@PathVariable Long eventId) {
        return "Event updated";
    }


    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("{eventId}/publish")
    public ResponseEntity<Void> publishEvent(@PathVariable Long eventId) {
        eventService.publishEvent(eventId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("@eventAuth.isOrganizer(#eventId, authentication)")
    @PutMapping("/{eventId}/ticket-categories")
    public ResponseEntity<?> syncTicketCategory(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketRequest request) {
        eventService.syncTicketCategory(eventId, request);

        return ResponseEntity.ok().build();
    }

}
