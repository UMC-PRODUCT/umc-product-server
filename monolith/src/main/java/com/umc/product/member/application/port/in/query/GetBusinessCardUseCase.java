package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.BusinessCardEditInfo;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo;

public interface GetBusinessCardUseCase {

    /**
     * 회원의 명함을 조회합니다. 회원이 없으면 예외를 던집니다.
     */
    BusinessCardInfo getByMemberId(Long memberId);

    /**
     * 회원의 명함 수정 화면 정보를 조회합니다. 회원이 없으면 예외를 던집니다.
     */
    BusinessCardEditInfo getEditByMemberId(Long memberId);
}
