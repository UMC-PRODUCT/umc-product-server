package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectMember.projectMember;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicantMatchingRoundInfo;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectMemberStatisticsQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 운영진: gisuId + chapterId 범위 내 ACTIVE (projectId, roundId, memberId) 목록. application inner join으로 랜덤
     * 매칭(application=null) 멤버를 자동 제외한다. 서비스에서 차수별·프로젝트×차수별·학교별 집계를 인메모리로 파생한다.
     */
    public List<ProjectApplicantMatchingRoundInfo> getMembersByRound(Long gisuId, Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectApplicantMatchingRoundInfo.class,
                project.id,
                projectMember.application.appliedMatchingRound.id,
                projectMember.memberId
            ))
            .from(projectMember)
            .join(projectMember.project, project)
            .join(projectMember.application, projectApplication)
            .where(
                project.gisuId.eq(gisuId),
                project.chapterId.eq(chapterId),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .fetch();
    }

    /**
     * PM챌린저: ownerMemberId 소유 프로젝트(gisuId + chapterId 범위)의 ACTIVE (projectId, roundId, memberId) 목록. application inner
     * join으로 랜덤 매칭(application=null) 멤버를 자동 제외한다.
     */
    public List<ProjectApplicantMatchingRoundInfo> getMembersByRoundForOwner(Long ownerMemberId, Long gisuId,
                                                                             Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                ProjectApplicantMatchingRoundInfo.class,
                project.id,
                projectMember.application.appliedMatchingRound.id,
                projectMember.memberId
            ))
            .from(projectMember)
            .join(projectMember.project, project)
            .join(projectMember.application, projectApplication)
            .where(
                project.productOwnerMemberId.eq(ownerMemberId),
                project.gisuId.eq(gisuId),
                project.chapterId.eq(chapterId),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .fetch();
    }
}
