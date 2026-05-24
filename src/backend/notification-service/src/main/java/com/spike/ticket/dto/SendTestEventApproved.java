package com.spike.ticket.dto;

import lombok.Data;

@Data
public class SendTestEventApproved {
   private String eventName;
   private String email;
   private String username;
}
