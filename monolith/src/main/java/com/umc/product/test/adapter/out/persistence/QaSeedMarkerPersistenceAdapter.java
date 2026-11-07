package com.umc.product.test.adapter.out.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.test.application.port.out.QaSeedMarkerPort;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class QaSeedMarkerPersistenceAdapter implements QaSeedMarkerPort {

    private final EntityManager entityManager;

    @Override
    public Set<String> findExistingNoticeTitles(Collection<String> titles) {
        return find("SELECT DISTINCT title FROM notice WHERE title IN (:values)", titles);
    }

    @Override
    public Set<String> findExistingScheduleNames(Collection<String> names) {
        return find("SELECT DISTINCT name FROM schedule WHERE name IN (:values)", names);
    }

    @Override
    public Set<String> findExistingThreadTitles(Collection<String> titles) {
        return find("SELECT DISTINCT title FROM community_thread WHERE title IN (:values)", titles);
    }

    @SuppressWarnings("unchecked")
    private Set<String> find(String sql, Collection<String> values) {
        if (values.isEmpty()) {
            return Set.of();
        }
        List<String> rows = entityManager.createNativeQuery(sql).setParameter("values", values).getResultList();
        return new HashSet<>(rows);
    }
}
