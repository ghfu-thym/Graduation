package com.spike.ticket.security;

import com.spike.ticket.enums.EventRole;
import com.spike.ticket.repository.EventMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("eventAuth")
@RequiredArgsConstructor
public class EventAuthLogic {

    private final EventMemberRepository eventMemberRepository;

    public boolean isOrganizer(Long eventId, JwtAuthenticationToken auth) {
        if (auth == null) return false;

        Long userId = Long.parseLong(auth.getToken().getSubject());
        return eventMemberRepository.existsByUserIdAndEventId(eventId, userId, EventRole.ORGANIZER);
    }

    public boolean isInspector( Long eventId, JwtAuthenticationToken auth) {
        if (auth == null) return false;

        Long userId = Long.parseLong(auth.getToken().getSubject());
        return eventMemberRepository.existsByUserIdAndEventId(eventId, userId, EventRole.INSPECTOR);
    }

    public boolean isAttendee(Long eventId, JwtAuthenticationToken auth) {
        if (auth == null) return false;

        Long userId = Long.parseLong(auth.getToken().getSubject());
        return eventMemberRepository.existsByUserIdAndEventId(eventId, userId, EventRole.ATTENDEE);
    }
}
