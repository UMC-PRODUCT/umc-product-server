package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.application.port.out.LoadTermsConsentPort;
import com.umc.product.terms.application.port.out.SaveTermsConsentPort;
import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TermsConsentPersistenceAdapter implements LoadTermsConsentPort, SaveTermsConsentPort {

    private final TermsConsentRepository repository;

    @Override
    public List<TermsConsent> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public Optional<TermsConsent> findByMemberIdAndTermType(Long memberId, TermsType termType) {
        return repository.findByMemberIdAndTermType(memberId, termType);
    }

    @Override
    public boolean existsByMemberIdAndTermType(Long memberId, TermsType termType) {
        return repository.existsByMemberIdAndTermType(memberId, termType);
    }

    @Override
    public TermsConsent save(TermsConsent termsConsent) {
        return repository.save(termsConsent);
    }

    @Override
    public void delete(TermsConsent termsConsent) {
        repository.delete(termsConsent);
    }
}
