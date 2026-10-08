package com.ecommerce.security.controller;

import com.ecommerce.security.dto.admin.AdminOrderDetailDTO;
import com.ecommerce.security.dto.admin.AdminOrderListDTO;
import com.ecommerce.security.dto.admin.AdminOrderStatusUpdateRequestDTO;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.service.AdminOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderListDTO>> getAllOrders() {
        return ResponseEntity.ok(adminOrderService.getAdminAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailDTO> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminOrderService.getAdminOrderById(id));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminOrderStatusUpdateRequestDTO request,
            org.springframework.security.core.Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = 1L;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                adminId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            }
            adminOrderService.updateOrderStatus(adminId, id, request.getStatus(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Order not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
