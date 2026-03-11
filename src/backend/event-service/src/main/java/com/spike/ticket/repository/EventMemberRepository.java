package com.spike.ticket.repository;

import com.spike.ticket.entity.EventMember;
import com.spike.ticket.enums.EventRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    //SELECT COUNT(*) FROM event_members WHERE event_id = ? AND user_id = ? AND role_type = ?
    boolean existsByUserIdAndEventId(Long userId, Long eventId, EventRole role);

}
