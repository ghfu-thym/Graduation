package com.spike.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class PresignedUrlResponse {
    private  String uploadUrl; // Link FE call PUT lên S3
    private  String fileKey;
}
