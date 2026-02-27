package com.umc.product.term.adapter.out.persistence;

import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.SaveTermConsentPort;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.enums.TermType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TermConsentPersistenceAdapter implements LoadTermConsentPort, SaveTermConsentPort {

    private final TermConsentRepository repository;

    @Override
    public List<TermConsent> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public Optional<TermConsent> findByMemberIdAndTermType(Long memberId, TermType termType) {
        return repository.findByMemberIdAndTermType(memberId, termType);
    }

    @Override
    public boolean existsByMemberIdAndTermType(Long memberId, TermType termType) {
        return repository.existsByMemberIdAndTermType(memberId, termType);
    }

    @Override
    public TermConsent save(TermConsent termConsent) {
        return repository.save(termConsent);
    }

    @Override
    public void delete(TermConsent termConsent) {
        repository.delete(termConsent);
    }
}
