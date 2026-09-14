package com.umc.product.term.application.port.in.query;

import java.util.List;

import com.umc.product.term.application.port.in.query.dto.TermInfo;

public interface GetTermAgreementUseCase {
    /**
     * 사용자가 동의한 약관들을 조회합니다.
     */
    List<TermInfo> getAgreedTermsByMemberId(Long memberId);
}
