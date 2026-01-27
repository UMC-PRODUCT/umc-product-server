package com.umc.product.terms.application.port.out;

import com.umc.product.terms.domain.TermsConsentLog;

public interface SaveTermsConsentLogPort {

    /**
     * 약관 동의/철회 로그를 저장합니다.
     */
    TermsConsentLog save(TermsConsentLog log);
}
