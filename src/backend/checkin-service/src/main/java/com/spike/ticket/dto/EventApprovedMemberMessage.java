package com.spike.ticket.dto;

import java.util.List;

public record EventApprovedMemberMessage(
        Long eventId,
        List<Long> memberIds
) {
}