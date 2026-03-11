package com.spike.ticket.controller;

import com.spike.ticket.dto.CreateEventRequest;
import com.spike.ticket.dto.EventResponse;
import com.spike.ticket.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class EventController {

    private final EventService eventService;

    /**
     * Tạo event mới (multipart/form-data vì có upload ảnh).
     * Dùng @ModelAttribute thay vì @RequestBody vì request chứa cả file + JSON fields.
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(
            @Valid @ModelAttribute CreateEventRequest request,
            JwtAuthenticationToken auth) {

        Long userId = Long.parseLong(auth.getToken().getSubject());
        EventResponse response = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

}
