package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProjectMember.projectMember;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ProjectMember QueryDSL 기반 조회 구현.
 */
@Repository
@RequiredArgsConstructor
public class ProjectMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 여러 프로젝트의 (projectId, part) 별 ACTIVE 멤버 수를 한 번에 집계한다.
     *
     * @return projectId -> (part -> 인원수) 맵. 인원이 0 인 (project, part) 조합은 엔트리 없음.
     */
    public Map<Long, Map<ChallengerPart, Long>> countByProjectIdsGroupByProjectIdAndPart(
        Collection<Long> projectIds
    ) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(projectMember.project.id, projectMember.part, projectMember.count())
            .from(projectMember)
            .where(
                projectMember.project.id.in(projectIds),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .groupBy(projectMember.project.id, projectMember.part)
            .fetch();

        Map<Long, Map<ChallengerPart, Long>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long projectId = row.get(projectMember.project.id);
            ChallengerPart part = row.get(projectMember.part);
            Long count = row.get(projectMember.count());
            result
                .computeIfAbsent(projectId, k -> new EnumMap<>(ChallengerPart.class))
                .put(part, count);
        }
        return result;
    }
}
