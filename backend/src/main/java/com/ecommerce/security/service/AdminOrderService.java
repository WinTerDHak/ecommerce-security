package com.ecommerce.security.service;

import com.ecommerce.security.dto.admin.*;
import com.ecommerce.security.entity.*;
import com.ecommerce.security.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public AdminOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentRepository paymentRepository, AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
    }

    public List<AdminOrderListDTO> getAdminAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(order -> {
            Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
            String paymentStatus = (payment != null) ? payment.getStatus().name() : "UNPAID";
            return new AdminOrderListDTO(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getUser().getFirstName() + " " + order.getUser().getLastName(),
                    order.getUser().getEmail(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    paymentStatus,
                    order.getCreatedAt() != null ? order.getCreatedAt().toString() : null
            );
        }).collect(Collectors.toList());
    }

    public AdminOrderDetailDTO getAdminOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
        String paymentStatus = (payment != null) ? payment.getStatus().name() : "UNPAID";
        String paymentRef = (payment != null) ? payment.getPaymentRef() : null;
        String paymentTime = (payment != null && payment.getCreatedAt() != null) ? payment.getCreatedAt().toString() : null;

        List<AdminOrderItemDTO> items = orderItemRepository.findByOrder_Id(order.getId()).stream()
                .map(item -> new AdminOrderItemDTO(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                )).collect(Collectors.toList());

        AdminOrderDetailDTO dto = new AdminOrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        dto.setCustomerEmail(order.getUser().getEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getStatus().name());
        dto.setPaymentStatus(paymentStatus);
        dto.setPaymentRef(paymentRef);
        dto.setPaymentTime(paymentTime);
        dto.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        dto.setItems(items);

        return dto;
    }

    @Transactional
    public void updateOrderStatus(Long adminId, Long orderId, String newStatusStr, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }

        OrderStatus oldStatus = order.getStatus();

        // Enforce valid transitions (or just log it depending on business logic, here we restrict invalid ones)
        if (oldStatus == OrderStatus.DELIVERED || oldStatus == OrderStatus.CANCELLED) {
            if (oldStatus != newStatus) {
                throw new IllegalArgumentException("Cannot change status from " + oldStatus);
            }
        }
        
        if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot skip PAID/SHIPPED directly to DELIVERED");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        auditLogService.logEvent(adminId, "ADMIN_ORDER_STATUS_UPDATE", "Order", order.getId(),
                "Status changed from " + oldStatus + " to " + newStatus, ipAddress);
    }
}
