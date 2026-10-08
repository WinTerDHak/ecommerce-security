package com.ecommerce.security.service;

import com.ecommerce.security.dto.admin.AdminUserDetailDTO;
import com.ecommerce.security.dto.admin.AdminUserListDTO;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.OrderRepository;
import com.ecommerce.security.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;

    public AdminUserService(UserRepository userRepository, OrderRepository orderRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.auditLogService = auditLogService;
    }

    public List<AdminUserListDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> new AdminUserListDTO(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        )).collect(Collectors.toList());
    }

    public AdminUserDetailDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        long orderCount = orderRepository.countByUser_Id(userId);

        return new AdminUserDetailDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null,
                orderCount
        );
    }

    @Transactional
    public void updateUserStatus(Long adminId, Long targetUserId, boolean newStatus, String ipAddress) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (adminId.equals(targetUserId)) {
            throw new SecurityException("Cannot disable your own account");
        }

        if (!newStatus && targetUser.getRole() == Role.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.isActive())
                    .count();
            if (adminCount <= 1) {
                throw new SecurityException("Cannot disable the last active administrator account");
            }
        }

        boolean oldStatus = targetUser.isActive();
        if (oldStatus != newStatus) {
            targetUser.setActive(newStatus);
            userRepository.save(targetUser);

            String statusStr = newStatus ? "ACTIVE" : "DISABLED";
            String oldStatusStr = oldStatus ? "ACTIVE" : "DISABLED";

            auditLogService.logEvent(adminId, "ADMIN_USER_STATUS_UPDATE", "User", targetUser.getId(),
                    "Status changed from " + oldStatusStr + " to " + statusStr, ipAddress);
        }
    }
}
