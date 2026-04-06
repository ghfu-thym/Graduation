package com.spike.ticket.repository;

import com.spike.ticket.entity.EventMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
