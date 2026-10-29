package com.umc.product.member.application.port.out;

import java.util.List;

import com.umc.product.member.application.port.out.dto.BusinessCardActivity;

/**
 * 명함에 표시할 회원의 UMC 활동 이력을 조회합니다.
 * <p>
 * 활동 이력은 challenger·organization 도메인 소유입니다. 해당 도메인의 계약이 바뀌면 구현 어댑터만 수정합니다.
 */
public interface LoadBusinessCardActivityPort {

    /**
     * @return 회원의 모든 챌린저 기록. 기록이 없으면 빈 목록
     */
    List<BusinessCardActivity> listByMemberId(Long memberId);
}
