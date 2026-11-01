package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectApplicationForm.projectApplicationForm;
import static com.umc.product.project.domain.QProjectMatchingRound.projectMatchingRound;
import static com.umc.product.project.domain.QProjectMember.projectMember;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchedMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsTargetProjectRow;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProjectStatisticsQueryRepository {

    private static final List<ProjectApplicationStatus> COUNTED_STATUSES = List.of(
        ProjectApplicationStatus.SUBMITTED,
        ProjectApplicationStatus.APPROVED,
        ProjectApplicationStatus.REJECTED
    );
    private static final List<ProjectStatus> PUBLIC_PROJECT_STATUSES = List.of(
        ProjectStatus.IN_PROGRESS,
        ProjectStatus.COMPLETED
    );
    private static final List<ChallengerPart> EXCLUDED_MATCHING_PARTS = List.of(
        ChallengerPart.PLAN,
        ChallengerPart.ADMIN
    );

    private final JPAQueryFactory queryFactory;

    public Optional<ProjectStatisticsProjectRow> findProjectById(Long projectId) {
        ProjectStatisticsProjectRow row = queryFactory
            .select(Projections.constructor(
                ProjectStatisticsProjectRow.class,
                project.id,
                project.gisuId,
                project.chapterId
            ))
            .from(project)
            .where(project.id.eq(projectId))
            .fetchOne();

        return Optional.ofNullable(row);
    }

    public List<ProjectStatisticsProjectRow> listProjectsByChapterId(Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsProjectRow.class,
                project.id,
                project.gisuId,
                project.chapterId
            ))
            .from(project)
            .where(project.chapterId.eq(chapterId))
            .orderBy(project.id.asc())
            .fetch();
    }

    /**
     * 지원 통계 대상 프로젝트를 조회한다.
     * <p>
     * 임시저장(DRAFT)·승인대기(PENDING_REVIEW) 프로젝트는 지원서를 받을 수 없어 제외한다.
     */
    public List<ProjectStatisticsTargetProjectRow> listApplicationStatisticsProjectsByIds(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsTargetProjectRow.class,
                project.id,
                project.name
            ))
            .from(project)
            .where(
                project.id.in(projectIds),
                project.status.notIn(ProjectStatus.DRAFT, ProjectStatus.PENDING_REVIEW)
            )
            .orderBy(project.submittedAt.asc().nullsFirst(), project.id.asc())
            .fetch();
    }

    public List<ProjectStatisticsTargetProjectRow> listPublicProjectsByChapterId(Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsTargetProjectRow.class,
                project.id,
                project.name
            ))
            .from(project)
            .where(
                project.chapterId.eq(chapterId),
                project.status.in(PUBLIC_PROJECT_STATUSES)
            )
            .orderBy(project.submittedAt.asc().nullsFirst(), project.id.asc())
            .fetch();
    }

    public List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsMemberRow.class,
                project.id,
                projectMember.id,
                projectMember.memberId,
                projectMember.part,
                projectMember.status
            ))
            .from(projectMember)
            .join(projectMember.project, project)
            .where(
                project.id.eq(projectId),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .orderBy(project.id.asc(), projectMember.id.asc())
            .fetch();
    }

    public List<ProjectStatisticsMatchedMemberRow> listPublicActiveMembersByChapterId(Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsMatchedMemberRow.class,
                project.id,
                projectMember.memberId,
                projectMember.part,
                projectMatchingRound.id
            ))
            .from(projectMember)
            .join(projectMember.project, project)
            .join(projectMember.assignedMatchingRound, projectMatchingRound)
            .where(
                project.chapterId.eq(chapterId),
                project.status.in(PUBLIC_PROJECT_STATUSES),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE),
                projectMember.part.notIn(EXCLUDED_MATCHING_PARTS)
            )
            .orderBy(project.id.asc(), projectMember.id.asc())
            .fetch();
    }

    public List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsApplicationRow.class,
                project.id,
                projectApplication.applicantMemberId,
                projectApplication.id,
                projectApplication.status,
                projectMatchingRound.id,
                projectMatchingRound.type,
                projectMatchingRound.phase
            ))
            .from(projectApplication)
            .join(projectApplication.applicationForm, projectApplicationForm)
            .join(projectApplicationForm.project, project)
            .join(projectApplication.appliedMatchingRound, projectMatchingRound)
            .where(
                project.id.in(projectIds),
                projectApplication.status.in(COUNTED_STATUSES)
            )
            .orderBy(
                project.id.asc(),
                projectApplication.applicantMemberId.asc(),
                projectMatchingRound.type.asc(),
                projectMatchingRound.phase.asc(),
                projectApplication.id.asc()
            )
            .fetch();
    }

}
