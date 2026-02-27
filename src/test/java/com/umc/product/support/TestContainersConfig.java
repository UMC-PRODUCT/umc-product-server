package com.umc.product.support;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgisContainer() {
        return new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:18-3.6").asCompatibleSubstituteFor("postgres")
        );
    }

    // 컨테이너가 뜬 뒤, 확장 1회 생성
    @Bean
    ApplicationRunner init(DataSource ds) {
        return args -> {
            try (var c = ds.getConnection(); var st = c.createStatement()) {
                st.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            }
        };
    }
}
