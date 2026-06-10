package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class MemberRoleMigrationTest {

    private static final String PREVIOUS_VERSION = "2026.05.26.10.00";
    private static final String MEMBER_ROLE_VERSION = "2026.06.03.00.00";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgis/postgis:18-3.6").asCompatibleSubstituteFor("postgres")
    );

    @Test
    void 기존_SUPER_ADMIN은_member_ADMIN으로_승격되고_challenger_role과_record에서_제거된다() throws Exception {
        migrateTo(PREVIOUS_VERSION);
        insertLegacyGlobalAdminRows();

        migrateTo(MEMBER_ROLE_VERSION);

        assertThat(selectString("SELECT role_type FROM public.member WHERE id = 100")).isEqualTo("ADMIN");
        assertThat(selectString("SELECT role_type FROM public.member WHERE id = 101")).isEqualTo("NORMAL");
        assertThat(selectLong("SELECT COUNT(*) FROM public.challenger_role WHERE role_type = 'SUPER_ADMIN'"))
            .isZero();
        assertThat(selectString("SELECT challenger_role_type FROM public.challenger_record WHERE id = 400"))
            .isNull();
        assertThat(selectObject("SELECT organization_id FROM public.challenger_record WHERE id = 400"))
            .isNull();
        assertThatThrownBy(() -> execute("""
            INSERT INTO public.challenger_role (
                id, challenger_id, gisu_id, organization_type, role_type, created_at, updated_at
            )
            VALUES (301, 201, 1, 'CENTRAL', 'SUPER_ADMIN', now(), now())
            """))
            .isInstanceOf(SQLException.class);
    }

    private void migrateTo(String version) {
        Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .target(version)
            .outOfOrder(true)
            .load()
            .migrate();
    }

    private void insertLegacyGlobalAdminRows() throws SQLException {
        execute("""
            INSERT INTO public.member (
                id, created_at, updated_at, nickname, status, name, email
            )
            VALUES
                (100, now(), now(), 'legacyAdmin', 'ACTIVE', 'Legacy Admin', 'legacy-admin@example.com'),
                (101, now(), now(), 'normalMember', 'ACTIVE', 'Normal Member', 'normal-member@example.com')
            """);
        execute("""
            INSERT INTO public.challenger (
                id, created_at, updated_at, gisu_id, member_id, part, status
            )
            VALUES
                (200, now(), now(), 1, 100, 'ADMIN', 'ACTIVE'),
                (201, now(), now(), 1, 101, 'WEB', 'ACTIVE')
            """);
        execute("""
            INSERT INTO public.challenger_role (
                id, challenger_id, gisu_id, organization_type, role_type, created_at, updated_at
            )
            VALUES (300, 200, 1, 'CENTRAL', 'SUPER_ADMIN', now(), now())
            """);
        execute("""
            INSERT INTO public.challenger_record (
                id, is_used, code, chapter_id, created_at, created_member_id, gisu_id,
                school_id, updated_at, challenger_role_type, organization_id
            )
            VALUES (400, false, 'MG0001', 1, now(), 100, 1, 1, now(), 'SUPER_ADMIN', 1)
            """);
    }

    private static void execute(String sql) throws SQLException {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static String selectString(String sql) throws SQLException {
        Object value = selectObject(sql);
        return value == null ? null : value.toString();
    }

    private static long selectLong(String sql) throws SQLException {
        Object value = selectObject(sql);
        return ((Number) value).longValue();
    }

    private static Object selectObject(String sql) throws SQLException {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getObject(1);
        }
    }
}
