package com.example.thexuong.service;

import com.example.thexuong.entity.SystemAuditLog;
import com.example.thexuong.repository.SystemAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SystemAuditLogRepository systemAuditLogRepository;

    @Transactional(readOnly = true)
    public List<SystemAuditLog> getAllAuditLogs() {
        return systemAuditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public void logAction(String module, String action, String targetId,
                          String oldValues, String newValues, String note) {
        String adminId = "SYSTEM";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                adminId = auth.getName();
            }
        } catch (Exception e) {
            // ignore
        }

        SystemAuditLog log = SystemAuditLog.builder()
                .adminId(adminId)
                .module(module)
                .action(action)
                .targetId(targetId)
                .oldValues(oldValues)
                .newValues(newValues)
                .changedFields(null)
                .note(note)
                .build();
        systemAuditLogRepository.save(log);
    }
}
