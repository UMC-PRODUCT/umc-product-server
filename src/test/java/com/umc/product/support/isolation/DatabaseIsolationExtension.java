package com.umc.product.support.isolation;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 각 Test의 AfterEach에 동작해서 DB 초기화를 수행합니다.
 */
class DatabaseIsolationExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        DatabaseManager databaseManager = getDatabaseManager(context);
        databaseManager.truncateTables();
    }

    private DatabaseManager getDatabaseManager(ExtensionContext context) {
        return (DatabaseManager) SpringExtension
            .getApplicationContext(context)
            .getBean("databaseManager");
    }
}
