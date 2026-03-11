package com.umc.product.audit.application.port.out;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLog;
import com.umc.product.global.exception.constant.Domain;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadAuditLogPort {
    Page<AuditLog> search(
        Domain domain, AuditAction action, Long actorMemberId,
        Instant from, Instant to, Pageable pageable
    );
}
