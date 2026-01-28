package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.application.port.out.SaveTermsConsentLogPort;
import com.umc.product.terms.domain.TermsConsentLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TermsConsentLogPersistenceAdapter implements SaveTermsConsentLogPort {

    private final TermsConsentLogRepository repository;

    @Override
    public TermsConsentLog save(TermsConsentLog log) {
        return repository.save(log);
    }
}
