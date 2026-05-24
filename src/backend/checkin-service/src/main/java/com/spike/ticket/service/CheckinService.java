package com.spike.ticket.service;

import com.spike.ticket.dto.CheckinResult;
import com.spike.ticket.dto.EventApprovedMemberMessage;
import com.spike.ticket.dto.TicketCreatedEvent;
import com.spike.ticket.dto.TicketDetail;
import com.spike.ticket.entity.EventMember;
import com.spike.ticket.entity.TicketCheckin;
import com.spike.ticket.enums.EventRole;
import com.spike.ticket.enums.TicketStatus;
import com.spike.ticket.repository.EventMemberRepository;
import com.spike.ticket.repository.TicketCheckinRepository;
import com.spike.ticket.utils.CheckinUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckinService {

    private final TicketCheckinRepository ticketCheckinRepository;
    private final CheckinUtils checkinUtils;
    private final EventMemberRepository eventMemberRepository;

    @Value("${app.ticket.hmac-secret}")
    private String secretKey;


    @Transactional
    public void createTicketCheckin(TicketCreatedEvent event) {

        for (TicketDetail ticketDetail : event.tickets()){
            TicketCheckin ticketCheckin = TicketCheckin.builder()
                    .ticketNumber(ticketDetail.ticketNumber())
                    .eventId(event.eventId())
                    .categoryId(ticketDetail.categoryId())
                    .status(TicketStatus.VALID)
                    .build();
            ticketCheckinRepository.save(ticketCheckin);
        }
    }

    // tận dụng lại CheckinResult
    @Transactional
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

    // lưu list các nhân viên của sự kiện, tạm thời không cần phân role inspector vs organizer nên để cả là inspector
    public void createEventMember(EventApprovedMemberMessage message) {
        List<String> memberEmailList = message.memberEmailList();
        for (String memberEmail : memberEmailList) {
            EventMember member = EventMember.builder()
                    .eventId(message.eventId())
                    .email(memberEmail)
                    .role(EventRole.INSPECTOR)
                    .build();
            eventMemberRepository.save(member);
        }
    }
}
