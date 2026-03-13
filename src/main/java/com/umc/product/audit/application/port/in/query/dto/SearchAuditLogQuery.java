package com.umc.product.audit.application.port.in.query.dto;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import java.time.Instant;

public record SearchAuditLogQuery(
    Domain domain,
    AuditAction action,
    Long actorMemberId,
    Instant from,
    Instant to
) {
}
