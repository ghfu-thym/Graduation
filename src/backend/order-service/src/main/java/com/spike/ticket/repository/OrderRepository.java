package com.spike.ticket.repository;

import com.spike.ticket.entity.Order;
import com.spike.ticket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Tìm đơn hàng theo transaction id, check xem có bị trùng đơn khi thanh toán
    Optional<Order> findByTxnId(String txnId);

    //Lịch sử mua hàng
    Page<Order> findByUserId(Long userID, Pageable pageable);

    // Tìm các đơn quá hạn
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoffTIme);

    // Xem chi tiết hơn hàng
    @Query("select o from Order o left join fetch o.orderItems where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Tìm băn tracking id
    Optional<Order> findByOrderTrackingNumber(String orderTrackingNumber);
}
