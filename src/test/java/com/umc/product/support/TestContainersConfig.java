package com.umc.product.support;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    private static final String TEST_POSTGIS_IMAGE_PROPERTY = "umc.test.postgis.image";
    private static final String TEST_POSTGIS_IMAGE_ENV = "UMC_TEST_POSTGIS_IMAGE";
    private static final String OFFICIAL_POSTGIS_IMAGE = "postgis/postgis:18-3.6";
    private static final String LOCAL_ARM64_POSTGIS_IMAGE = "umc-product-postgis-test:18.2-postgis";
    private static final Path ARM64_POSTGIS_DOCKERFILE = Path.of("docker/test/postgis/Dockerfile");

    private static final DockerImageName POSTGIS_IMAGE = resolvePostgisImage();

    private static final PostgreSQLContainer<?> POSTGIS_CONTAINER = startPostgisContainer();

    private static final AtomicInteger DATABASE_SEQUENCE = new AtomicInteger();

    @Bean
    JdbcConnectionDetails postgisConnectionDetails() {
        String databaseName = "test_" + DATABASE_SEQUENCE.incrementAndGet();
        createDatabase(databaseName);
        String jdbcUrl = "jdbc:postgresql://%s:%d/%s?loggerLevel=OFF".formatted(
            POSTGIS_CONTAINER.getHost(),
            POSTGIS_CONTAINER.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            databaseName
        );
        return new JdbcConnectionDetails() {
            @Override
            public String getUsername() {
                return POSTGIS_CONTAINER.getUsername();
            }

            @Override
            public String getPassword() {
                return POSTGIS_CONTAINER.getPassword();
            }

            @Override
            public String getJdbcUrl() {
                return jdbcUrl;
            }
        };
    }

    // 각 테스트 데이터베이스에 PostGIS 확장을 생성한다.
    @Bean
    ApplicationRunner init(DataSource ds) {
        return args -> {
            try (var c = ds.getConnection(); var st = c.createStatement()) {
                st.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            }
        };
    }

    private static DockerImageName resolvePostgisImage() {
        String configuredImage = configuredPostgisImage();
        if (configuredImage != null) {
            return postgresCompatibleImage(configuredImage);
        }
        if (isArm64()) {
            return buildArm64PostgisImage();
        }
        return postgresCompatibleImage(OFFICIAL_POSTGIS_IMAGE);
    }

    private static String configuredPostgisImage() {
        String image = System.getProperty(TEST_POSTGIS_IMAGE_PROPERTY);
        if (image == null || image.isBlank()) {
            image = System.getenv(TEST_POSTGIS_IMAGE_ENV);
        }
        return image == null || image.isBlank() ? null : image.trim();
    }

    private static boolean isArm64() {
        String osArch = System.getProperty("os.arch");
        return "aarch64".equals(osArch) || "arm64".equals(osArch);
    }

    private static PostgreSQLContainer<?> startPostgisContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGIS_IMAGE);
        container.start();
        return container;
    }

    private static DockerImageName buildArm64PostgisImage() {
        try {
            String imageName = new ImageFromDockerfile(LOCAL_ARM64_POSTGIS_IMAGE, false)
                .withDockerfile(ARM64_POSTGIS_DOCKERFILE)
                .get();
            return postgresCompatibleImage(imageName);
        } catch (RuntimeException e) {
            throw new IllegalStateException("arm64 테스트 환경용 PostGIS 이미지를 빌드하지 못했습니다.", e);
        }
    }

    private static DockerImageName postgresCompatibleImage(String imageName) {
        return DockerImageName.parse(imageName).asCompatibleSubstituteFor("postgres");
    }

    private static void createDatabase(String databaseName) {
        try (
            var connection = DriverManager.getConnection(
                POSTGIS_CONTAINER.getJdbcUrl(),
                POSTGIS_CONTAINER.getUsername(),
                POSTGIS_CONTAINER.getPassword()
            );
            var statement = connection.createStatement()
        ) {
            statement.execute("CREATE DATABASE " + databaseName);
        } catch (SQLException e) {
            throw new IllegalStateException("테스트 데이터베이스를 생성하지 못했습니다: " + databaseName, e);
        }
    }
}
