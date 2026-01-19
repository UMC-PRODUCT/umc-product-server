package com.umc.product.terms.application.port.out;

import com.umc.product.terms.domain.TermsConsent;

public interface SaveTermsConsentPort {
    /**
     * 약관 동의 정보를 저장합니다.
     */
    TermsConsent save(TermsConsent termsConsent);

    /**
     * 약관 동의 정보를 삭제합니다.
     */
    void delete(TermsConsent termsConsent);
}
