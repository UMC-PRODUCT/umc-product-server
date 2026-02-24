package com.umc.product.term.application.port.out;

import com.umc.product.term.domain.TermConsent;

public interface SaveTermConsentPort {
    /**
     * 약관 동의 정보를 저장합니다.
     */
    TermConsent save(TermConsent termConsent);

    /**
     * 약관 동의 정보를 삭제합니다.
     */
    void delete(TermConsent termConsent);
}
