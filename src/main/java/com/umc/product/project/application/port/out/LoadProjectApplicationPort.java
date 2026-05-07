package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.Optional;

/**
 * {@code findBy*} — 없어도 정상 ({@link Optional})
 * {@code getBy*} — 반드시 있어야 하며 없으면 {@code ProjectDomainException}
 */
public interface LoadProjectApplicationPort {

    boolean existsByAppliedMatchingRoundId(Long matchingRoundId);

    /**
     * (projectId, applicantMemberId, status) 조합으로 본인 지원서를 조회합니다.
     * 멱등 처리(APPLY-001) 및 본인 지원서 식별(APPLY-002/003)에 사용됩니다.
     */
    Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndStatus(
        Long projectId, Long applicantMemberId, ProjectApplicationStatus status
    );

    /**
     * (projectId, applicantMemberId, roundId, status) 조합으로 조회합니다.
     * 현재 오픈된 차수 기준으로 기존 DRAFT를 찾을 때 사용합니다.
     */
    Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndRoundIdAndStatus(
        Long projectId, Long applicantMemberId, Long roundId, ProjectApplicationStatus status
    );

    /**
     * 본인의 DRAFT 지원서를 반드시 존재하는 것으로 조회합니다. 없으면 {@code PROJECT_APPLICATION_NOT_FOUND} 예외.
     * update / submit 에서 사용합니다.
     */
    ProjectApplication getDraftByProjectAndMember(Long projectId, Long memberId);

    /**
     * 동일 차수에 이미 제출된 지원서가 있는지 확인합니다. (중복 제출 방지용)
     */
    boolean existsByRoundAndApplicantAndStatus(
        Long roundId, Long applicantMemberId, ProjectApplicationStatus status
    );
}
