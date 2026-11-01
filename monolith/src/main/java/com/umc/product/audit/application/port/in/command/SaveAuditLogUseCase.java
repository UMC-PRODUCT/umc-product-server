package com.umc.product.audit.application.port.in.command;

import com.umc.product.audit.domain.AuditLogEvent;

public interface SaveAuditLogUseCase {
    void save(AuditLogEvent event);
}
