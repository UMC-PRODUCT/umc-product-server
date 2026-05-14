package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectApplicationForm.projectApplicationForm;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectApplicationStatisticsQueryRepository {

    private static final List<ProjectApplicationStatus> COUNTED_STATUSES = List.of(
        ProjectApplicationStatus.SUBMITTED,
        ProjectApplicationStatus.APPROVED,
        ProjectApplicationStatus.REJECTED
    );

    private final JPAQueryFactory queryFactory;

    /**
     * gisuId + chapterId 범위 내 (projectId, roundId, applicantMemberId) 목록.
     * 한 지원자가 같은 차수에 여러 프로젝트에 지원한 경우 projectId별로 각각 포함된다.
     * 서비스에서 roundId·(projectId,roundId)·(schoolId,roundId) 집계를 인메모리로 파생한다.
     */
    public List<RoundMemberInfo> listApplicantsByRound(Long gisuId, Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                RoundMemberInfo.class,
                project.id,
                projectApplication.appliedMatchingRound.id,
                projectApplication.applicantMemberId
            ))
            .from(projectApplication)
            .join(projectApplication.applicationForm, projectApplicationForm)
            .join(projectApplicationForm.project, project)
            .where(
                project.gisuId.eq(gisuId),
                project.chapterId.eq(chapterId),
                projectApplication.status.in(COUNTED_STATUSES)
            )
            .fetch();
    }

    /** 단일 프로젝트의 (projectId, roundId, applicantMemberId) 목록. */
    public List<RoundMemberInfo> listApplicantsByRoundForProject(Long projectId) {
        return queryFactory
            .select(Projections.constructor(
                RoundMemberInfo.class,
                projectApplicationForm.project.id,
                projectApplication.appliedMatchingRound.id,
                projectApplication.applicantMemberId
            ))
            .from(projectApplication)
            .join(projectApplication.applicationForm, projectApplicationForm)
            .where(
                projectApplicationForm.project.id.eq(projectId),
                projectApplication.status.in(COUNTED_STATUSES)
            )
            .fetch();
    }
}
