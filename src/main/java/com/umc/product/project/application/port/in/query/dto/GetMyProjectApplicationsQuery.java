package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import lombok.Builder;

/**
 * 본인 지원 내역 목록 조회 Query.
 *
 * @param requesterMemberId 요청자 Member ID
 * @param gisuId            대상 기수 ID. 사용자의 파트 결정 및 프로젝트 기수 필터에 사용된다.
 * @param status            상태 필터. {@code null} 이면 PENDING(임시저장)을 제외한 전체 (SUBMITTED/APPROVED/REJECTED). 명시 시 해당 상태만
 *                          조회된다.
 */
@Builder
public record GetMyProjectApplicationsQuery(
    Long requesterMemberId,
    Long gisuId,
    ProjectApplicationStatus status
) {
}
