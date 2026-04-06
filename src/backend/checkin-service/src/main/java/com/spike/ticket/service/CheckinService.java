package com.spike.ticket.service;

import com.spike.ticket.dto.CheckinResult;
import com.spike.ticket.dto.TicketCreatedEvent;
import com.spike.ticket.entity.TicketCheckin;
import com.spike.ticket.enums.TicketStatus;
import com.spike.ticket.repository.TicketCheckinRepository;
import com.spike.ticket.utils.CheckinUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckinService {

    private final TicketCheckinRepository ticketCheckinRepository;
    private final CheckinUtils checkinUtils;

    @Value("${app.ticket.hmac-secret}")
    private String secretKey;


    public void createTicketCheckin(TicketCreatedEvent event) {
        TicketCheckin ticketCheckin = TicketCheckin.builder()
                .ticketNumber(event.ticketNumber())
                .eventId(event.eventId())
                .categoryId(event.categoryId())
                .status(TicketStatus.VALID)
                .build();
        ticketCheckinRepository.save(ticketCheckin);
    }

    // tận dụng lại CheckinResult
    public CheckinResult processCheckin(String qrData, Long eventId) {
        CheckinResult verifyResult = checkinUtils.verifyQrData(qrData,secretKey);
        String ticketNumber = verifyResult.getTicketNumber();
        if(!verifyResult.isSuccess()){
           return new CheckinResult(false, ticketNumber,verifyResult.getMessage());
        }
        int rowAffected = ticketCheckinRepository.scanAndLockTicket(ticketNumber, eventId);
        if(rowAffected == 0){
            return new CheckinResult(false, ticketNumber,"Vé đã được check in hoặc sai sự kiện");
        }
        return new CheckinResult(true, ticketNumber, verifyResult.getMessage());
    }
}
