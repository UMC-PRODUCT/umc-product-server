package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * 지원서 시나리오 시딩 결과.
 *
 * @param submittedCount SUBMITTED 까지 완료된 지원서 수
 * @param approvedCount  APPROVED 처리된 지원서 수 (ProjectMember 생성 포함)
 * @param rejectedCount  REJECTED 처리된 지원서 수
 * @param failures       단계 중간에 실패한 건 목록
 */
public record SeedProjectApplicationsResult(
    int submittedCount,
    int approvedCount,
    int rejectedCount,
    List<FailedApplication> failures
) {

    /**
     * 중간 단계에서 실패한 지원서 건.
     *
     * @param applicantMemberId 지원자 Member ID
     * @param projectId         지원 시도한 프로젝트 ID. Draft 생성 전 실패한 경우 null.
     * @param failedStep        실패 단계 (DRAFT / FILL / SUBMIT / DECIDE / ADD_MEMBER)
     * @param reason            실패 원인 메시지
     */
    public record FailedApplication(
        Long applicantMemberId,
        Long projectId,
        String failedStep,
        String reason
    ) {

    }
}
