package com.spike.ticket.controller;

import com.spike.ticket.dto.CheckinDto;
import com.spike.ticket.dto.CheckinResult;
import com.spike.ticket.service.CheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {
    private final CheckinService checkinService;

    @PostMapping("/scan")
    @PreAuthorize("@checkInAuth.canCheckIn(#request.eventId, authentication)")
    public ResponseEntity<CheckinDto.ScanResponse> scanTicket(
            @Valid @RequestBody CheckinDto.ScanRequest request,
            JwtAuthenticationToken auth
    ){
        String inspectorId = auth.getToken().getSubject();

        // HMAC + lock db
        CheckinResult result  = checkinService.processCheckin(request.getQrData(), request.getEventId());

        if (result.isSuccess()){
            return ResponseEntity.ok(new CheckinDto.ScanResponse(true, result.getMessage()));
        } else {
            return ResponseEntity.ok(new CheckinDto.ScanResponse(false, result.getMessage()));
        }
    }
        //TODO: đưa event member từ event service sang, set quyền

}
