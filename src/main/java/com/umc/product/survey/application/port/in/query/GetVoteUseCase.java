package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.VoteInfo;

/**
 * 설문 도메인의 투표 정보를 조회하기 위한 유스케이스
 */
public interface GetVoteUseCase {

    /**
     * 특정 폼(투표)의 상세 결과 및 사용자의 선택 내역을 조회합니다.
     *
     * @param formId   조회할 폼 ID
     * @param memberId 조회하는 회원 ID (본인의 선택 내역 확인용)
     * @return 투표 상세 정보 DTO
     */
    VoteInfo getVoteInfo(Long formId, Long memberId);
}
