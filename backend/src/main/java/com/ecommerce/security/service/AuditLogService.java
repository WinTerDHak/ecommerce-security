package com.ecommerce.security.service;

import com.ecommerce.security.entity.AuditLog;
import com.ecommerce.security.repository.AuditLogRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logEvent(Long userId, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            log.setUser(user);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}

