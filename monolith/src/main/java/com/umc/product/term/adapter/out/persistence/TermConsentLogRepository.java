package com.umc.product.term.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.term.domain.TermConsentLog;

public interface TermConsentLogRepository extends JpaRepository<TermConsentLog, Long> {
}
