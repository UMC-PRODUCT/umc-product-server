package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectApplicationForm.projectApplicationForm;
import static com.umc.product.project.domain.QProjectMatchingRound.projectMatchingRound;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ProjectApplication QueryDSL 동적 검색 구현.
 */
@Repository
@RequiredArgsConstructor
public class ProjectApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** (projectId, applicantMemberId, roundId, status) 조합으로 단건 조회. */
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

    /** 동일 차수에 특정 상태의 지원서가 존재하는지 확인. */
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
     * (projectId, applicantMemberId, status) 조합으로 단건 조회.
     * applicationForm.project.id 를 통해 projectId 까지 타고 들어간다.
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
     * applicationForm -> project, appliedMatchingRound 를 fetch join 으로 함께 로드한다. 정렬: matching round startsAt ASC ->
     * application updatedAt DESC.
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
     * status null -> PENDING(임시저장) 제외, 명시 시 정확히 그 상태.
     */
    private BooleanExpression statusCond(ProjectApplicationStatus status) {
        return status == null
            ? projectApplication.status.ne(ProjectApplicationStatus.PENDING)
            : projectApplication.status.eq(status);
    }
}
