package com.umc.product.test.adapter.out.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.domain.startingline.OwnedProject;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * 리허설 판단용 네이티브 SELECT. 쓰기는 하지 않는다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SeedProjectReadPersistenceAdapter implements SeedProjectReadPort {

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<OwnedProject> listOwnedProjects(Long gisuId, Collection<Long> ownerMemberIds) {
        if (ownerMemberIds.isEmpty()) {
            return List.of();
        }
        List<Object[]> projects = rows("""
            SELECT DISTINCT ON (p.product_owner_member_id)
                   p.id, p.product_owner_member_id, p.status,
                   COALESCE(p.name, '') <> '',
                   EXISTS (SELECT 1 FROM project_application_form paf WHERE paf.project_id = p.id)
            FROM project p
            WHERE p.gisu_id = :gisuId
              AND p.product_owner_member_id IN (:ownerIds)
              AND p.status IN ('DRAFT', 'PENDING_REVIEW', 'IN_PROGRESS')
            ORDER BY p.product_owner_member_id, p.id
            """, Map.of("gisuId", gisuId, "ownerIds", ownerMemberIds));
        if (projects.isEmpty()) {
            return List.of();
        }
        List<Long> projectIds = projects.stream().map(row -> toLong(row[0])).toList();
        Map<Long, Map<ChallengerPart, Integer>> quotas = new HashMap<>();
        for (Object[] row : rows("""
            SELECT ppq.project_id, ppq.part, ppq.quota FROM project_part_quota ppq WHERE ppq.project_id IN (:ids)
            """, Map.of("ids", projectIds))) {
            quotas.computeIfAbsent(toLong(row[0]), id -> new EnumMap<>(ChallengerPart.class))
                .put(ChallengerPart.valueOf((String) row[1]), (int) toLong(row[2]).longValue());
        }
        List<OwnedProject> result = new ArrayList<>();
        for (Object[] row : projects) {
            Long projectId = toLong(row[0]);
            result.add(new OwnedProject(projectId, toLong(row[1]), ProjectStatus.valueOf((String) row[2]),
                (Boolean) row[3], (Boolean) row[4], quotas.getOrDefault(projectId, Map.of())));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(String sql, Map<String, Object> parameters) {
        var query = entityManager.createNativeQuery(sql);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
