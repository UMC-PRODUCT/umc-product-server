package com.umc.product.audit.adapter.out.persistence;

import com.umc.product.audit.application.port.out.LoadAuditLogPort;
import com.umc.product.audit.application.port.out.SaveAuditLogPort;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLog;
import com.umc.product.global.exception.constant.Domain;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements SaveAuditLogPort, LoadAuditLogPort {

    private final AuditLogJpaRepository repository;
    private final AuditLogQueryRepository queryRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return repository.save(auditLog);
    }

    @Override
    public Page<AuditLog> search(
        Domain domain, AuditAction action, Long actorMemberId,
        Instant from, Instant to, Pageable pageable
    ) {
        return queryRepository.search(domain, action, actorMemberId, from, to, pageable);
    }
}
