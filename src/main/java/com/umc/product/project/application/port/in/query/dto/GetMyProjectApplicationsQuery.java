package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;

import lombok.Builder;

/**
 * 본인 지원 내역 목록 조회 Query.
 *
 * @param requesterMemberId 요청자 Member ID
 * @param gisuId            대상 기수 ID. 사용자의 파트 결정 및 프로젝트 기수 필터에 사용된다.
 * @param status            지원서 카드의 상태 필터. {@code null} 이면 DRAFT(임시저장)을 제외한 지원서와 랜덤 매칭 카드를 함께 조회한다. 값을 보내면 해당 상태를
 *                          확인할 수 있는 지원서 카드만 조회한다. 랜덤 매칭 카드는 지원서 상태 필터에 대응하지 않아 포함하지 않는다.
 */
@Builder
public record GetMyProjectApplicationsQuery(
    Long requesterMemberId,
    Long gisuId,
    ProjectApplicationStatus status
) {
}
