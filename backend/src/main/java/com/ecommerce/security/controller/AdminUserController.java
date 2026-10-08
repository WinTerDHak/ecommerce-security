package com.ecommerce.security.controller;

import com.ecommerce.security.dto.admin.AdminUserDetailDTO;
import com.ecommerce.security.dto.admin.AdminUserListDTO;
import com.ecommerce.security.dto.admin.AdminUserStatusUpdateRequestDTO;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserListDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailDTO> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminUserService.getUserById(id));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusUpdateRequestDTO request,
            org.springframework.security.core.Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = 1L;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                adminId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            }
            adminUserService.updateUserStatus(adminId, id, request.getActive(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
