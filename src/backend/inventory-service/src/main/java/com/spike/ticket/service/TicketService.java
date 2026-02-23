package com.spike.ticket.service;


import com.spike.ticket.enums.TicketStatus;
import com.spike.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;

    @Transactional
    public boolean reserveTicket(List<Long> seatIds) {
        for (Long ticketId : seatIds) {
            // 1. Cố gắng chuyển trạng thái từ AVAILABLE sang RESERVED
            // Hàm này trả về số dòng update được (1 là thành công, 0 là thất bại)
            int updatedRows = ticketRepository.updateStatus(ticketId, TicketStatus.AVAILABLE, TicketStatus.RESERVED);

            if (updatedRows == 0) {
                // Nếu update thất bại -> Nghĩa là ghế đã bị người khác lấy mất
                // Phải rollback lại toàn bộ (nhả các ghế đã giữ trước đó trong vòng lặp)
                throw new RuntimeException("Ghế " + ticketId + " đã không còn trống!");
            }
        }
        return true; // Giữ chỗ thành công tất cả
    }
}
