package com.ecommerce.security.controller;

import com.ecommerce.security.dto.order.OrderRequestDTO;
import com.ecommerce.security.dto.order.OrderResponseDTO;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(orderService.createOrder(userDetails.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(userDetails.getId(), id));
    }
}
