package com.ecommerce.security.repository;
import com.ecommerce.security.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    Optional<Payment> findByOrder_Id(Long orderId);
}

