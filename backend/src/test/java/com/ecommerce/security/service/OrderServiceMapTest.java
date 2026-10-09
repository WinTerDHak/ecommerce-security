package com.ecommerce.security.service;

import com.ecommerce.security.dto.order.OrderResponseDTO;
import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderServiceMapTest {

    @Test
    public void testMapToDTO_includesShippingAddress() throws Exception {
        // Create an Order Service instance with nulls (we just test the private mapping method)
        OrderService orderService = new OrderService(null, null, null, null, null, null);

        Order order = new Order();
        order.setId(99L);
        order.setOrderNumber("ORD-123");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setShippingStreet("123 Test St");
        order.setShippingCity("Test City");
        order.setShippingZip("12345");
        order.setShippingCountry("Test Country");

        // Use reflection to call the private mapToDTO method
        Method mapMethod = OrderService.class.getDeclaredMethod("mapToDTO", Order.class);
        mapMethod.setAccessible(true);
        OrderResponseDTO dto = (OrderResponseDTO) mapMethod.invoke(orderService, order);

        assertEquals("123 Test St", dto.getShippingStreet());
        assertEquals("Test City", dto.getShippingCity());
        assertEquals("12345", dto.getShippingZip());
        assertEquals("Test Country", dto.getShippingCountry());
    }
}
