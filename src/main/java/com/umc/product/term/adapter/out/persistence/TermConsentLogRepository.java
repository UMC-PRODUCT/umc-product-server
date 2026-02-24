package com.umc.product.term.adapter.out.persistence;

import com.umc.product.term.domain.TermConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermConsentLogRepository extends JpaRepository<TermConsentLog, Long> {
}
