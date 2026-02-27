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
        this.tableNames = tableNameExtractor.getNames();
    }

    public void truncateTables() {
        String tables = String.join(", ", tableNames);
        entityManager.createNativeQuery("TRUNCATE TABLE " + tables + " RESTART IDENTITY CASCADE")
                .executeUpdate();
    }
}
