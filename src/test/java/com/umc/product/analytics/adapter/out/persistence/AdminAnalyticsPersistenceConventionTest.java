package com.umc.product.analytics.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AdminAnalyticsPersistenceConvention")
class AdminAnalyticsPersistenceConventionTest {

    private static final Path ANALYTICS_PERSISTENCE_PATH = Path.of(
        "src/main/java/com/umc/product/analytics/adapter/out/persistence"
    );

    @Test
    @DisplayName("analytics query repository는 native SQL 대신 QueryDSL을 사용한다")
    void analytics_query_repository는_native_SQL_대신_QueryDSL을_사용한다() throws IOException {
        List<Path> queryRepositories = Files.list(ANALYTICS_PERSISTENCE_PATH)
            .filter(path -> path.getFileName().toString().endsWith("AnalyticsQueryRepository.java"))
            .toList();

        assertThat(queryRepositories).hasSize(4);

        for (Path queryRepository : queryRepositories) {
            String source = Files.readString(queryRepository);

            assertThat(source)
                .as("%s", queryRepository)
                .contains("JPAQueryFactory")
                .doesNotContain("EntityManager")
                .doesNotContain("createNativeQuery")
                .doesNotContain("jakarta.persistence.Query");
        }
    }
}
