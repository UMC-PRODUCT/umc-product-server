package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingAffiliationInfo;

public interface GetMyMatchingAffiliationUseCase {

    /**
     * 기수 내 본인의 원지부와 매칭 지부를 조회합니다.
     *
     * @param memberId 조회하는 회원 ID
     * @param gisuId   기수 ID. null 이면 활성 기수
     */
    ProjectMatchingAffiliationInfo get(Long memberId, Long gisuId);
}
