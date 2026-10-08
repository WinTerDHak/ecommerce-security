package com.ecommerce.security.repository;
import com.ecommerce.security.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    java.util.List<OrderItem> findByOrder_Id(Long orderId);
}
