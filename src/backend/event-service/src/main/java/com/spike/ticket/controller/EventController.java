package com.spike.ticket.controller;

import com.spike.ticket.dto.CreateEventRequest;
import com.spike.ticket.dto.CreateTicketRequest;
import com.spike.ticket.dto.EventResponse;
import com.spike.ticket.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;


    @PostMapping(value = "/create")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @ModelAttribute CreateEventRequest request,
            JwtAuthenticationToken auth) {
        log.info("Create event request: {}", request);

        Long userId = Long.parseLong(auth.getToken().getSubject());
        String username = auth.getToken().getClaimAsString("username");
        String email = auth.getToken().getClaimAsString("email");
        EventResponse response = eventService.createEvent(userId,username, email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PreAuthorize("@eventAuth.isOrganizer(#eventId, authentication)")
    @PostMapping("{eventId}/inspectors")
    public String addInspector(
            @PathVariable Long eventId,
            @RequestParam List<Long> userIds) {
        eventService.addInspector(eventId, userIds);

        return String.format("%d Inspectors added to event", userIds.size());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("{eventId}/publish")
    public ResponseEntity<Void> publishEvent(@PathVariable Long eventId) {
        eventService.publishEvent(eventId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("@eventAuth.isOrganizer(#eventId, authentication)")
    @PutMapping("/{eventId}/ticket-categoties")
    public ResponseEntity<?> syncTicketCategory(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketRequest request) {
        eventService.syncTicketCategory(eventId, request);

        return ResponseEntity.ok().build();
    }

}
