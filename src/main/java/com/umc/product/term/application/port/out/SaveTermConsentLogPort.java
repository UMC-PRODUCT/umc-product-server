package com.umc.product.term.application.port.out;

import com.umc.product.term.domain.TermConsentLog;

public interface SaveTermConsentLogPort {

    /**
     * 약관 동의/철회 로그를 저장합니다.
     */
    TermConsentLog save(TermConsentLog log);
}
