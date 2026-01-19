package com.umc.product.terms.application.port.in.query;

import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import com.umc.product.terms.domain.enums.TermsType;

public interface GetTermsUseCase {
    /**
     * 선택된 약관 유형에서 활성화된 약관을 가져옵니다.
     * <p>
     * 여러 개일 경우 가장 최신 것을 가져옴.
     */
    TermsInfo getTermsByType(TermsType type);

    /**
     * ID로 약관 정보를 가져옵니다.
     */
    TermsInfo getTermsById(Long termsId);
}
