package com.umc.product.support.isolation;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class PostgreSqlTableNameExtractor implements TableNameExtractor {

    private final EntityManager entityManager;

    public PostgreSqlTableNameExtractor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getNames() {
        return entityManager.createNativeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'")
            .getResultList()
            .stream()
            // Flyway 마이그레이션 테이블을 제외하고 가져가도록 합니다.
            .filter(name -> !name.equals("flyway_schema_history"))
            .toList();
    }
}
