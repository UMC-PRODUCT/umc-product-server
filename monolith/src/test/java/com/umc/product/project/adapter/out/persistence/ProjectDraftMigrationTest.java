package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
class ProjectDraftMigrationTest {

    private static final String MIGRATION =
        "db/migration/V2026.10.24.00.00__drop_project_draft_unique_indexes.sql";

    @Autowired
    JdbcConnectionDetails connectionDetails;

    @Test
    void 기존_데이터와_이력을_보존하며_초안_유일성_제약만_해제한다() throws Exception {
        // given — 공용 DB가 아니라 Testcontainers 내부에 이 테스트 전용 DB를 생성한다.
        String database = "draft_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        String originalUrl = connectionDetails.getJdbcUrl();
        String url = originalUrl.substring(0, originalUrl.lastIndexOf('/') + 1) + database;
        try (Connection admin = DriverManager.getConnection(originalUrl,
            connectionDetails.getUsername(), connectionDetails.getPassword())) {
            admin.createStatement().execute("CREATE DATABASE " + database);
            try {
                var source = new DriverManagerDataSource(url,
                    connectionDetails.getUsername(), connectionDetails.getPassword());
                JdbcTemplate jdbc = new JdbcTemplate(source);
                Flyway old = Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .target(MigrationVersion.fromVersion("2026.10.23.00.00")).load();
                old.migrate();
                insert(jdbc, 11L, 11L, "DRAFT");
                insert(jdbc, 11L, 11L, "PENDING_REVIEW");
                insert(jdbc, 11L, 11L, "IN_PROGRESS");
                List<Map<String, Object>> existing = jdbc.queryForList("SELECT * FROM project ORDER BY id");
                List<Map<String, Object>> history = jdbc.queryForList(
                    "SELECT version, checksum FROM flyway_schema_history WHERE success ORDER BY installed_rank");
                assertOldConstraints(jdbc);

                // when — SQL이 트랜잭션 안에서 롤백 가능한지도 실제 PostgreSQL에서 확인한다.
                try (Connection transaction = source.getConnection()) {
                    transaction.setAutoCommit(false);
                    try {
                        ScriptUtils.executeSqlScript(transaction, new ClassPathResource(MIGRATION));
                    } finally {
                        transaction.rollback();
                    }
                }
                assertOldConstraints(jdbc);
                assertThat(jdbc.queryForList("SELECT * FROM project ORDER BY id")).isEqualTo(existing);

                Flyway upgraded = Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .target(MigrationVersion.fromVersion("2026.10.24.00.00")).load();
                assertThat(upgraded.migrate().migrationsExecuted).isEqualTo(1);
                upgraded.validate();

                // then — 데이터와 적용된 기존 파일의 checksum을 유지하고, 복수 초안만 새로 허용한다.
                assertThat(jdbc.queryForList("SELECT * FROM project ORDER BY id")).isEqualTo(existing);
                assertThat(jdbc.queryForList(
                    "SELECT version, checksum FROM flyway_schema_history WHERE success ORDER BY installed_rank"))
                    .containsAll(history).hasSize(history.size() + 1);
                assertThat(jdbc.queryForList("""
                    SELECT indexname FROM pg_indexes
                    WHERE tablename = 'project'
                    AND indexname IN ('uk_project_creator_gisu_draft', 'uk_project_owner_gisu_draft')
                    """)).isEmpty();
                insert(jdbc, 11L, 11L, "DRAFT");
                insert(jdbc, 12L, 11L, "DRAFT");
                insert(jdbc, 11L, 12L, "DRAFT");
                assertThat(jdbc.queryForObject("SELECT count(*) FROM project", Long.class)).isEqualTo(existing.size() + 3L);
                assertThat(jdbc.queryForObject("""
                    SELECT indexdef FROM pg_indexes
                    WHERE indexname = 'idx_project_creator_gisu_draft_created_at'
                    """, String.class)).doesNotContain("UNIQUE");
            } finally {
                admin.createStatement().execute("DROP DATABASE " + database + " WITH (FORCE)");
            }
        }
    }

    private void assertOldConstraints(JdbcTemplate jdbc) {
        assertThatThrownBy(() -> insert(jdbc, 11L, 12L, "DRAFT"))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insert(jdbc, 12L, 11L, "DRAFT"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insert(JdbcTemplate jdbc, Long creator, Long owner, String status) {
        jdbc.update("""
            INSERT INTO project (created_at, updated_at, gisu_id, chapter_id, status, name,
                product_owner_member_id, product_owner_school_id, created_by_member_id)
            VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, ?, '기존 프로젝트', ?, 1, ?)
            """, status, owner, creator);
    }
}
