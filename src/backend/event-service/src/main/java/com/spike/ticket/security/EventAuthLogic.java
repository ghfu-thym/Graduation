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
        String email = auth.getToken().getClaimAsString("email");
        return eventMemberRepository.existsByUserEmailAndEventId(email, eventId, EventRole.ORGANIZER);
    }

    public boolean isInspector( Long eventId, JwtAuthenticationToken auth) {
        if (auth == null) return false;

        String email = auth.getToken().getClaimAsString("email");
        return eventMemberRepository.existsByUserEmailAndEventId(email, eventId, EventRole.INSPECTOR);
    }

    public boolean isAttendee(Long eventId, JwtAuthenticationToken auth) {
        if (auth == null) return false;

        String email = auth.getToken().getClaimAsString("email");
        return eventMemberRepository.existsByUserEmailAndEventId(email, eventId, EventRole.ATTENDEE);
    }
}
