package com.ecommerce.security.controller;

import com.ecommerce.security.dto.admin.AdminOrderStatusUpdateRequestDTO;
import com.ecommerce.security.entity.Order;
import com.ecommerce.security.entity.OrderStatus;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingOrderId;

    @BeforeEach
    void setUp() {
        if (orderRepository.count() == 0) {
            User user = userRepository.findByEmail("customer@ecommerce.local").orElseThrow();
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setShippingStreet("123 Street");
            order.setShippingCity("City");
            order.setShippingZip("10000");
            order.setShippingCountry("Country");
            order.setTotalAmount(new BigDecimal("100000"));
            order = orderRepository.save(order);
            existingOrderId = order.getId();
        } else {
            existingOrderId = orderRepository.findAll().get(0).getId();
            Order order = orderRepository.findById(existingOrderId).get();
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
        }
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanListOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanViewOrderDetail() throws Exception {
        mockMvc.perform(get("/api/admin/orders/" + existingOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingOrderId));
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void adminCanUpdateOrderStatus() throws Exception {
        AdminOrderStatusUpdateRequestDTO request = new AdminOrderStatusUpdateRequestDTO();
        request.setStatus("PAID");

        mockMvc.perform(put("/api/admin/orders/" + existingOrderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer@ecommerce.local", roles = {"USER"})
    void customerCannotAccessAdminOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessAdminOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void nonexistentOrderReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/orders/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@ecommerce.local", roles = {"ADMIN"})
    void invalidStatusTransitionRejected() throws Exception {
        Order order = orderRepository.findById(existingOrderId).get();
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        AdminOrderStatusUpdateRequestDTO request = new AdminOrderStatusUpdateRequestDTO();
        request.setStatus("DELIVERED"); // Cannot jump from PENDING to DELIVERED directly

        mockMvc.perform(put("/api/admin/orders/" + existingOrderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
