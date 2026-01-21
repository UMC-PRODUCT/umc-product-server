package com.umc.product.terms.application.port.in.query;

import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import java.util.List;

public interface GetTermsAgreementUseCase {
    /**
     * 사용자가 동의한 약관들을 조회합니다.
     */
    List<TermsInfo> getAgreedTermsByMemberId(Long memberId);
}
