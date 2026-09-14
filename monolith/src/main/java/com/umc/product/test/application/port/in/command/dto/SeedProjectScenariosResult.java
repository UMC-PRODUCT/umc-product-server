package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

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
     * @param partFills         {@link TargetProjectStatus#IN_PROGRESS} 에서만 not null.
     *                          {@code filled == 0} 인 entry 도 포함된다(의도된 0 / 풀 부족 0 구분 정보).
     */
    public record CreatedProject(
        Long projectId,
        TargetProjectStatus finalStatus,
        Long productOwnerMemberId,
        Long chapterId,
        Long schoolId,
        Long applicationFormId,
        List<PartFill> partFills
    ) {
    }

    /**
     * 한 파트의 quota 와 실제 추가된 멤버 수.
     */
    public record PartFill(ChallengerPart part, long quota, long filled) {
    }

    /**
     * 중간 단계에서 실패한 프로젝트.
     *
     * @param projectId       create 까지는 성공해 ID 가 발급된 경우 not null
     * @param reachedStatus   현재 DB 에 남아있는 상태. create 자체가 실패했으면 null
     * @param intendedStatus  의도했던 최종 상태
     * @param failedStep      실패한 단계 식별자 (CREATE_DRAFT, UPDATE, FORM, SUBMIT, QUOTA, PUBLISH, FILL_MEMBERS)
     * @param reason          실패 원인 메시지
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
