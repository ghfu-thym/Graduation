package com.spike.ticket.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Primary
public class TicketClientMock implements TicketClient{
    @Override
    public Boolean reserveTicket(List<Long> ticketIds) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.info("Mock: Reserve ticket failed");
        }
        return true;
    }
}
