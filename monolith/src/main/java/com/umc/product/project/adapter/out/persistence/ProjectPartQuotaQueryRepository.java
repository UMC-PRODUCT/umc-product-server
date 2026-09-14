package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProjectPartQuota.projectPartQuota;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ProjectPartQuota QueryDSL 기반 조회 구현.
 */
@Repository
@RequiredArgsConstructor
public class ProjectPartQuotaQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 여러 프로젝트의 partQuota 를 한 번에 조회해 projectId 기준으로 그룹핑한다.
     * <p>
     * {@code projectPartQuota.project.id} projection 으로 FK 만 추출하여, 호출자가 {@code quota.getProject().getId()} 로 lazy 프록시에
     * 접근하지 않도록 한다.
     */
    public Map<Long, List<ProjectPartQuota>> listByProjectIdsGroupedByProjectId(
        Collection<Long> projectIds
    ) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(projectPartQuota.project.id, projectPartQuota)
            .from(projectPartQuota)
            .where(projectPartQuota.project.id.in(projectIds))
            .fetch();

        Map<Long, List<ProjectPartQuota>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long projectId = row.get(projectPartQuota.project.id);
            ProjectPartQuota quota = row.get(projectPartQuota);
            result.computeIfAbsent(projectId, k -> new ArrayList<>()).add(quota);
        }
        return result;
    }
}
