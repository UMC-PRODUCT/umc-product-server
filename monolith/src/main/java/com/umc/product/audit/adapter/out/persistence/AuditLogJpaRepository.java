package com.umc.product.audit.adapter.out.persistence;

import com.umc.product.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
}
