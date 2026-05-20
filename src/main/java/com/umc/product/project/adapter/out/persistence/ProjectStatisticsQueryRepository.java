package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectApplicationForm.projectApplicationForm;
import static com.umc.product.project.domain.QProjectMatchingRound.projectMatchingRound;
import static com.umc.product.project.domain.QProjectMember.projectMember;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectStatisticsQueryRepository {

    private static final List<ProjectApplicationStatus> COUNTED_STATUSES = List.of(
        ProjectApplicationStatus.SUBMITTED,
        ProjectApplicationStatus.APPROVED,
        ProjectApplicationStatus.REJECTED
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

    public List<ProjectStatisticsMatchingRoundRow> listMatchingRoundsByChapterId(Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectStatisticsMatchingRoundRow.class,
                projectMatchingRound.id,
                projectMatchingRound.type,
                projectMatchingRound.phase
            ))
            .from(projectMatchingRound)
            .where(projectMatchingRound.chapterId.eq(chapterId))
            .orderBy(projectMatchingRound.startsAt.asc(), projectMatchingRound.id.asc())
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

    public List<ProjectStatisticsMemberRow> listActiveMembersByChapterId(Long chapterId) {
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
                project.chapterId.eq(chapterId),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
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
