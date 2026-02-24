package com.umc.product.term.application.port.in.query;

import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.domain.enums.TermType;
import java.util.Set;

public interface GetTermUseCase {
    /**
     * 선택된 약관 유형에서 활성화된 약관을 가져옵니다.
     * <p>
     * 여러 개일 경우 가장 최신 것을 가져옴.
     */
    TermInfo getTermsByType(TermType type);

    /**
     * ID로 약관 정보를 가져옵니다.
     */
    TermInfo getTermsById(Long termsId);

    /**
     * 현재 활성화된 필수 약관 ID 목록을 조회합니다.
     */
    Set<Long> getRequiredTermIds();
}
