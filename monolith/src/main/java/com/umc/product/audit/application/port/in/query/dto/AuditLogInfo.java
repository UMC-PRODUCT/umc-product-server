package com.umc.product.audit.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLog;
import com.umc.product.global.exception.constant.Domain;

public record AuditLogInfo(
    Long id,
    Domain domain,
    AuditAction action,
    String targetType,
    String targetId,
    Long actorMemberId,
    String description,
    String details,
    String ipAddress,
    Instant createdAt
) {
    public static AuditLogInfo from(AuditLog auditLog) {
        return new AuditLogInfo(
            auditLog.getId(),
            auditLog.getDomain(),
            auditLog.getAction(),
            auditLog.getTargetType(),
            auditLog.getTargetId(),
            auditLog.getActorMemberId(),
            auditLog.getDescription(),
            auditLog.getDetails(),
            auditLog.getIpAddress(),
            auditLog.getCreatedAt()
        );
    }
}
