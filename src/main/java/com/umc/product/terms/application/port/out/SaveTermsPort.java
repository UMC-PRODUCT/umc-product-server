package com.umc.product.terms.application.port.out;

import com.umc.product.terms.domain.Terms;

public interface SaveTermsPort {
    /**
     * 약관을 저장합니다.
     */
    Terms save(Terms terms);

    /**
     * 약관을 삭제합니다.
     */
    void delete(Terms terms);
}
