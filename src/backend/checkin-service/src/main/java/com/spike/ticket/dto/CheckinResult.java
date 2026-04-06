package com.spike.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CheckinResult {
    boolean success;
    String ticketNumber;
    String message;
}
