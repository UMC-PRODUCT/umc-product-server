package com.umc.product.audit.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.audit.domain.AuditLog;

public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
}
