package com.spike.ticket.security;

import com.spike.ticket.repository.EventMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("checkInAuth")
@RequiredArgsConstructor
public class CheckInAuthLogic {
    private final EventMemberRepository eventMemberRepository;

    public boolean canCheckIn(Long eventId, JwtAuthenticationToken auth) {

        if (auth == null || auth.getToken() == null) return false;

        Long userId = Long.parseLong(auth.getToken().getSubject());

        return eventMemberRepository.existsByUserIdAndEventId(userId, eventId);
    }
}
