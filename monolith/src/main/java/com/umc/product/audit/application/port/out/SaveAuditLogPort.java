package com.umc.product.audit.application.port.out;

import com.umc.product.audit.domain.AuditLog;

public interface SaveAuditLogPort {
    AuditLog save(AuditLog auditLog);
}
