package com.spike.ticket.repository;

import com.spike.ticket.entity.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, Long> {
    boolean existsByOrderTrackingNumber(String orderTrackingNumber);
}
