package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * 지원서 시나리오 시딩 결과.
 *
 * @param matchingRoundId      사용된 매칭 차수 ID
 * @param createdApplications  성공한 지원서 — 프로젝트별로 그룹핑된 목록
 * @param failedApplications   단계 중간에 실패한 건 목록
 * @param counts               전체 통계 요약
 */
public record SeedProjectApplicationsResult(
    Long matchingRoundId,
    List<ProjectApplications> createdApplications,
    List<FailedApplication> failedApplications,
    Counts counts
) {

    /**
     * 한 프로젝트에 들어간 지원서 묶음.
     */
    public record ProjectApplications(
        Long projectId,
        List<ApplicationEntry> applications
    ) {

    }

    /**
     * 성공한 지원서 1건의 최종 정보.
     *
     * @param applicationId     ProjectApplication ID
     * @param applicantMemberId 지원자 Member ID
     * @param part              지원 파트 (= 챌린저 본인의 part)
     * @param finalStatus       최종 status (SUBMITTED / APPROVED / REJECTED)
     */
    public record ApplicationEntry(
        Long applicationId,
        Long applicantMemberId,
        ChallengerPart part,
        ProjectApplicationStatus finalStatus
    ) {

    }

    /**
     * 시딩 호출 전체에 대한 카운트.
     *
     * @param submittedTotal 최종 SUBMITTED 상태로 남은 건 수
     * @param approvedTotal  최종 APPROVED 처리된 건 수
     * @param rejectedTotal  최종 REJECTED 처리된 건 수
     * @param failedTotal    단계 중간에 실패한 건 수
     */
    public record Counts(
        int submittedTotal,
        int approvedTotal,
        int rejectedTotal,
        int failedTotal
    ) {

    }

    /**
     * 중간 단계에서 실패한 지원서 건.
     *
     * @param applicantMemberId 지원자 Member ID
     * @param projectId         지원 시도한 프로젝트 ID. NO_PROJECT 의 경우 null.
     * @param applicationId     ProjectApplication ID. DRAFT 단계 실패 시 null.
     * @param failedStep        실패 단계 (NO_PROJECT / DRAFT / FILL / SUBMIT / DECIDE)
     * @param reason            실패 원인 메시지
     */
    public record FailedApplication(
        Long applicantMemberId,
        Long projectId,
        Long applicationId,
        String failedStep,
        String reason
    ) {

    }
}
