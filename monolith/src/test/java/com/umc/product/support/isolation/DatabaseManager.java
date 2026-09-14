package com.umc.product.support.isolation;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
class DatabaseManager {

    private final EntityManager entityManager;
    private final List<String> tableNames;

    public DatabaseManager(EntityManager entityManager, TableNameExtractor tableNameExtractor) {
        this.entityManager = entityManager;
        // 초기화 대상인 테이블 이름들을 가지고 옵니다.
        this.tableNames = tableNameExtractor.getNames();
    }

    public void truncateTables() {
        String tables = String.join(", ", tableNames);
        entityManager.createNativeQuery("TRUNCATE TABLE " + tables + " RESTART IDENTITY CASCADE")
            .executeUpdate();
    }
}
