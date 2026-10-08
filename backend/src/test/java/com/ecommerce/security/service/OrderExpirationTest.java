package com.ecommerce.security.service;

import com.ecommerce.security.entity.*;
import com.ecommerce.security.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OrderExpirationTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private AuditLogService auditLogService;

    private OrderService orderService;
    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    
    // Tracking method calls manually for AuditLogService
    private int auditLogCount = 0;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        auditLogService = new AuditLogService(null, null) {
            @Override
            public void logEvent(Long userId, String action, String entityType, Long entityId, String details, String ipAddress) {
                auditLogCount++;
            }
        };

        orderService = new OrderService(orderRepository, orderItemRepository, cartItemRepository,
                productRepository, userRepository, auditLogService);

        testUser = new User();
        testUser.setId(1L);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setStockQuantity(5);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(LocalDateTime.now().minusMinutes(20));

        testOrderItem = new OrderItem();
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
    }

    @Test
    void testCancelExpiredOrders_cancelsPendingAndRestoresStock() {
        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of(testOrderItem));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));

        orderService.cancelExpiredOrders();

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        assertEquals(7, testProduct.getStockQuantity()); // Restored 5 + 2

        verify(productRepository, times(1)).save(testProduct);
        verify(orderRepository, times(1)).save(testOrder);
        assertEquals(1, auditLogCount);
    }

    @Test
    void testCancelExpiredOrders_ignoresNonPendingOrders() {
        testOrder.setStatus(OrderStatus.PAID);
        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(testOrder));

        orderService.cancelExpiredOrders();

        assertEquals(OrderStatus.PAID, testOrder.getStatus());
        verify(productRepository, never()).findByIdForUpdate(any());
        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        assertEquals(0, auditLogCount);
    }

    @Test
    void testCancelExpiredOrders_idempotentStockRestore() {
        // If someone called it twice on the same reference but it got cancelled in between
        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of(testOrderItem));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));

        // First call
        orderService.cancelExpiredOrders();
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        assertEquals(7, testProduct.getStockQuantity());

        // Second call with same order mock (which is now CANCELLED)
        orderService.cancelExpiredOrders();
        
        // Stock should still be 7, save operations still 1
        assertEquals(7, testProduct.getStockQuantity());
        verify(productRepository, times(1)).save(any());
        verify(orderRepository, times(1)).save(any());
    }
}
