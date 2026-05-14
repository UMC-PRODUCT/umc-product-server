package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProject.project;
import static com.umc.product.project.domain.QProjectApplication.projectApplication;
import static com.umc.product.project.domain.QProjectMember.projectMember;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectMemberStatisticsQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * gisuId + chapterId 범위 내 (projectId, roundId, memberId) 목록.
     * ACTIVE ProjectMember 중 application != null 인 경우만 포함.
     * 서비스에서 차수별·프로젝트×차수별·학교별 집계를 인메모리로 파생한다.
     */
    public List<RoundMemberInfo> listMembersByRound(Long gisuId, Long chapterId) {
        return queryFactory
            .select(Projections.constructor(
                RoundMemberInfo.class,
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
}
