package com.ecommerce.security.service;

import com.ecommerce.security.dto.order.OrderRequestDTO;
import com.ecommerce.security.dto.order.OrderResponseDTO;
import com.ecommerce.security.entity.*;
import com.ecommerce.security.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, 
                        CartItemRepository cartItemRepository, ProductRepository productRepository, 
                        UserRepository userRepository, AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OrderResponseDTO createOrder(Long userId, OrderRequestDTO request) {
        User user = userRepository.getReferenceById(userId);
        List<CartItem> cartItems = cartItemRepository.findByUser_Id(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingStreet(request.getShippingStreet());
        order.setShippingCity(request.getShippingCity());
        order.setShippingZip(request.getShippingZip());
        order.setShippingCountry(request.getShippingCountry());
        order.setTotalAmount(BigDecimal.ZERO); // Temporary, update after calculating items

        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            // Pessimistic lock on product to avoid concurrent checkout issues
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            
            orderItemRepository.save(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);

        return mapToDTO(order);
    }

    public List<OrderResponseDTO> getUserOrders(Long userId) {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: You do not own this order");
        }

        return mapToDTO(order);
    }

    private OrderResponseDTO mapToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : null
        );
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, threshold);

        for (Order order : expiredOrders) {
            if (order.getStatus() != OrderStatus.PENDING) {
                continue;
            }

            order.setStatus(OrderStatus.CANCELLED);

            List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
            for (OrderItem item : items) {
                Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalStateException("Product not found"));
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }

            orderRepository.save(order);
            auditLogService.logEvent(order.getUser().getId(), "ORDER_CANCELLED_AUTO", "Order", order.getId(), "Order automatically cancelled due to payment timeout", "SYSTEM");
        }
    }
}
