package com.ecommerce.security.repository;
import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);
    long countByUser_Id(Long userId);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);
}

