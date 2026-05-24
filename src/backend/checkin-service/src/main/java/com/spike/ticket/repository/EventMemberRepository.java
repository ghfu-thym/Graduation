package com.spike.ticket.repository;

import com.spike.ticket.entity.EventMember;
import com.spike.ticket.enums.EventRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    //SELECT COUNT(*) FROM event_members WHERE event_id = ? AND email = ? AND role_type = ?
    @Query("select case when count(em) > 0 then true else false end " +
            "from EventMember em " +
            "where em.email = :email and em.eventId = :eventId")
    boolean existsByUserEmailAndEventId(@Param("email") String email,
                                        @Param("eventId") Long eventId);
}
