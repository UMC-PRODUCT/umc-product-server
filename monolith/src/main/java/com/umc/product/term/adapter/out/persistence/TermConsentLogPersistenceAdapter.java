package com.umc.product.term.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.umc.product.term.application.port.out.SaveTermConsentLogPort;
import com.umc.product.term.domain.TermConsentLog;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TermConsentLogPersistenceAdapter implements SaveTermConsentLogPort {

    private final TermConsentLogRepository repository;

    @Override
    public TermConsentLog save(TermConsentLog log) {
        return repository.save(log);
    }
}
