package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 시나리오 시딩 결과.
 *
 * @param createdProjects {@code targetStatus} 까지 도달한 프로젝트 목록
 * @param failedProjects  단계 중간에 실패해 {@code targetStatus} 에 도달하지 못한 프로젝트 목록.
 *                        중간까지 진행된 데이터(예: DRAFT) 는 DB 에 남아있을 수 있으므로 호출자가
 *                        orphan 정리 여부를 판단해야 한다.
 */
public record SeedProjectScenariosResult(
    List<CreatedProject> createdProjects,
    List<FailedProject> failedProjects
) {

    /**
     * 시나리오의 모든 단계가 성공한 프로젝트.
     *
     * @param applicationFormId {@link TargetProjectStatus#PENDING_REVIEW} 이상에서만 not null
     * @param partQuotas        설정한 파트별 TO. {@link TargetProjectStatus#IN_PROGRESS} 에서만 채워지고 그 외에는 빈 목록
     */
    public record CreatedProject(
        Long projectId,
        TargetProjectStatus finalStatus,
        Long productOwnerMemberId,
        Long chapterId,
        Long schoolId,
        Long applicationFormId,
        List<PartQuota> partQuotas
    ) {
    }

    public record PartQuota(ChallengerPart part, long quota) {
    }

    /**
     * 중간 단계에서 실패한 프로젝트.
     *
     * @param projectId      create 까지는 성공해 ID 가 발급된 경우 not null
     * @param reachedStatus  현재 DB 에 남아있는 상태. create 전에 실패했으면 null
     * @param intendedStatus 의도했던 최종 상태
     * @param failedStep     실패한 단계 식별자 (CREATE_DRAFT, UPDATE, FORM, QUOTA, SUBMIT).
     *                       QUOTA 이면서 projectId 가 null 이면 지부에 남은 TO 가 없어 만들지 않은 것이다
     * @param reason         실패 원인 메시지
     */
    public record FailedProject(
        Long projectId,
        TargetProjectStatus reachedStatus,
        TargetProjectStatus intendedStatus,
        String failedStep,
        String reason
    ) {
    }
}
