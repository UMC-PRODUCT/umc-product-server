package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.domain.TermsConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsConsentLogRepository extends JpaRepository<TermsConsentLog, Long> {
}
