package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectApplicationForm.projectApplicationForm;
import static com.umc.product.project.domain.QProjectMatchingRound.projectMatchingRound;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

import lombok.RequiredArgsConstructor;

/**
 * ProjectApplication QueryDSL 동적 검색 구현.
 */
@Repository
@RequiredArgsConstructor
public class ProjectApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * (projectId, applicantMemberId, roundId, status) 조합으로 단건 조회.
     */
    public Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndRoundIdAndStatus(
        Long projectId, Long applicantMemberId, Long roundId, ProjectApplicationStatus status
    ) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(projectApplication)
                .where(
                    projectApplication.applicationForm.project.id.eq(projectId),
                    projectApplication.applicantMemberId.eq(applicantMemberId),
                    projectApplication.appliedMatchingRound.id.eq(roundId),
                    projectApplication.status.eq(status)
                )
                .fetchOne()
        );
    }

    /**
     * 동일 차수에 특정 상태의 지원서가 존재하는지 확인.
     */
    public boolean existsByRoundAndApplicantAndStatus(
        Long roundId, Long applicantMemberId, ProjectApplicationStatus status
    ) {
        return queryFactory
            .selectOne()
            .from(projectApplication)
            .where(
                projectApplication.appliedMatchingRound.id.eq(roundId),
                projectApplication.applicantMemberId.eq(applicantMemberId),
                projectApplication.status.eq(status)
            )
            .fetchFirst() != null;
    }

    /**
     * (projectId, applicantMemberId, status) 조합으로 단건 조회. applicationForm.project.id 를 통해 projectId 까지 타고 들어간다.
     */
    public Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndStatus(
        Long projectId, Long applicantMemberId, ProjectApplicationStatus status
    ) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(projectApplication)
                .where(
                    projectApplication.applicationForm.project.id.eq(projectId),
                    projectApplication.applicantMemberId.eq(applicantMemberId),
                    projectApplication.status.eq(status)
                )
                .fetchOne()
        );
    }

    /**
     * 본인 지원 내역을 조회한다.
     * <p>
     * applicationForm -> project, appliedMatchingRound 를 fetch join 으로 함께 로드한다.
     * <p>
     * 정렬: matching round startsAt ASC -> application updatedAt DESC.
     */
    public List<ProjectApplication> searchMyApplications(
        Long applicantMemberId,
        Long gisuId,
        MatchingType matchingType,
        ProjectApplicationStatus status
    ) {
        return queryFactory
            .selectFrom(projectApplication)
            .innerJoin(projectApplication.applicationForm, projectApplicationForm).fetchJoin()
            .innerJoin(projectApplicationForm.project, project).fetchJoin()
            .innerJoin(projectApplication.appliedMatchingRound, projectMatchingRound).fetchJoin()
            .where(
                projectApplication.applicantMemberId.eq(applicantMemberId),
                project.gisuId.eq(gisuId),
                projectMatchingRound.type.eq(matchingType),
                statusCond(status)
            )
            .orderBy(projectMatchingRound.startsAt.asc(), projectApplication.updatedAt.desc())
            .fetch();
    }

    /**
     * PM/운영진용 단일 프로젝트의 지원자 목록을 조회한다.
     * <p>
     * applicationForm -> project, appliedMatchingRound 를 fetch join 으로 함께 로드한다.
     * 호출자({@code ProjectApplicationSummaryInfo#from})가 form.project.id 까지 접근하므로 N+1 방지를 위해 fetch join 이 필요하다.
     * 임시저장(DRAFT)은 항상 제외된다.
     * <p>
     * 정렬: matchingRound.phase ASC -> projectApplication.submittedAt ASC.
     */
    public List<ProjectApplication> searchProjectApplications(
        Long projectId,
        Long matchingRoundId,
        ProjectApplicationStatus status
    ) {
        return queryFactory
            .selectFrom(projectApplication)
            .innerJoin(projectApplication.applicationForm, projectApplicationForm).fetchJoin()
            .innerJoin(projectApplicationForm.project, project).fetchJoin()
            .innerJoin(projectApplication.appliedMatchingRound, projectMatchingRound).fetchJoin()
            .where(
                projectApplicationForm.project.id.eq(projectId),
                matchingRoundIdEq(matchingRoundId),
                managedStatusCond(status)
            )
            .orderBy(projectMatchingRound.phase.asc(), projectApplication.submittedAt.asc())
            .fetch();
    }

    /**
     * 단건 상세 조회 — applicationForm/project, appliedMatchingRound 를 fetch join 한다.
     * <p>
     * formResponse 는 별도 도메인이라 본 쿼리에서 fetch 하지 않는다 — 호출자가 cross-domain UseCase 로 로드한다.
     */
    public Optional<ProjectApplication> findByIdWithDetails(Long applicationId) {
        ProjectApplication result = queryFactory
            .selectFrom(projectApplication)
            .innerJoin(projectApplication.applicationForm, projectApplicationForm).fetchJoin()
            .innerJoin(projectApplicationForm.project, project).fetchJoin()
            .innerJoin(projectApplication.appliedMatchingRound, projectMatchingRound).fetchJoin()
            .where(projectApplication.id.eq(applicationId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    private BooleanExpression matchingRoundIdEq(Long matchingRoundId) {
        return matchingRoundId == null ? null : projectMatchingRound.id.eq(matchingRoundId);
    }

    /**
     * PM/운영진 영역 status 필터.
     * <ul>
     *   <li>null -> SUBMITTED/APPROVED/REJECTED 전체 (DRAFT 제외)</li>
     *   <li>명시 -> 해당 상태 단일</li>
     * </ul>
     * Query 단에서 DRAFT 는 사전 차단되므로 (도메인 invariant), 여기서는 그대로 eq 만 적용한다.
     */
    private BooleanExpression managedStatusCond(ProjectApplicationStatus status) {
        return status == null
            ? projectApplication.status.in(List.of(
            ProjectApplicationStatus.SUBMITTED,
            ProjectApplicationStatus.APPROVED,
            ProjectApplicationStatus.REJECTED))
            : projectApplication.status.eq(status);
    }

    /**
     * status null -> DRAFT(임시저장) 제외, 명시 시 정확히 그 상태.
     */
    private BooleanExpression statusCond(ProjectApplicationStatus status) {
        return status == null
            ? projectApplication.status.ne(ProjectApplicationStatus.DRAFT)
            : projectApplication.status.eq(status);
    }
}
