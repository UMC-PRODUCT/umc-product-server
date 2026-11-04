package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.support.IntegrationTestSupport;

/**
 * 빈 DB에서 QA 계정 → 프로젝트 출발선 → 매칭 차수 하나의 봇 지원·결정까지 리허설을 한 번 돈다. 테스트가 끝나면 골격까지
 * 지워지므로, 이 클래스만 쓰는 속성으로 새 컨텍스트(새 DB)를 받고 시나리오를 한 테스트에 담는다.
 */
@TestPropertySource(properties = {
    "app.seed.enabled=true",
    "app.seed.email-domain=qa.test",
    "test.context.key=matching-rehearsal"
})
@Import(MatchingRehearsalIntegrationTest.CheapPasswordEncoderConfig.class)
class MatchingRehearsalIntegrationTest extends IntegrationTestSupport {

    private static final List<String> PROJECT_TABLES = List.of(
        "project", "project_part_quota", "project_application_form", "form", "question", "project_member",
        "project_application", "form_response", "answer", "answer_choice");

    @Test
    @DisplayName("QA 계정 다음 출발선을 만들면 지부마다 공개·승인 대기·무프로젝트 PLAN이 나뉘고, 다시 부르면 아무것도 바꾸지 않는다")
    void rehearsalFromEmptyDatabase() throws Exception {
        // given
        JsonNode qa = post("/test/seed/qa").path("steps");
        assertThat(qa.findValuesAsText("status")).as(qa.toPrettyString()).doesNotContain("FAILED", "NOT_RUN");

        // when
        JsonNode line = post("/test/seed/projects/starting-line");

        // then
        assertThat(line.path("failed").asInt()).as(line.toPrettyString()).isZero();
        assertThat(line.path("applied").asInt()).isEqualTo(59);
        assertThat(countByChapterAndStatus()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "캥거루:IN_PROGRESS", 9L, "쿼카:IN_PROGRESS", 15L, "수달:IN_PROGRESS", 9L, "아홀로틀:IN_PROGRESS", 9L,
            "티라노:IN_PROGRESS", 7L, "캥거루:PENDING_REVIEW", 2L, "쿼카:PENDING_REVIEW", 2L, "수달:PENDING_REVIEW", 2L,
            "아홀로틀:PENDING_REVIEW", 2L, "티라노:PENDING_REVIEW", 2L));
        assertThat(count("""
            SELECT count(*) FROM project_part_quota q JOIN project p ON p.id = q.project_id
            WHERE p.status = 'PENDING_REVIEW'
            """)).isZero();
        assertThat(count("""
            SELECT count(*) FROM project p
            WHERE (SELECT count(*) FROM project_part_quota q
                   WHERE q.project_id = p.id AND q.part IN ('WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER')) <> 1
              AND p.status = 'IN_PROGRESS'
            """)).isZero();
        JsonNode kangaroo = line.path("chapters").get(0);
        assertThat(kangaroo.path("chapter").asText()).isEqualTo("캥거루");
        assertThat(List.of("DESIGN", "WEB_PRODUCT_ENGINEER", "MOBILE_PRODUCT_ENGINEER").stream()
            .map(part -> kangaroo.path("quotaTotals").path(part).asInt())).containsExactly(8, 37, 13);
        assertThat(kangaroo.path("withoutProject").toString())
            .isEqualTo("[\"hansung_g11_qa13@qa.test\",\"hansung_g11_qa26@qa.test\"]");
        Map<String, Long> afterLine = tableCounts();

        // when
        JsonNode again = post("/test/seed/projects/starting-line");

        // then
        assertThat(again.path("unchanged").asInt()).as(again.toPrettyString()).isEqualTo(59);
        assertThat(tableCounts()).isEqualTo(afterLine);
    }

    private JsonNode post(String path) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post(path))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("result");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> countByChapterAndStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT c.name, p.status, count(*) FROM project p JOIN chapter c ON c.id = p.chapter_id GROUP BY c.name, p.status
            """).getResultList();
        rows.forEach(row -> counts.put(row[0] + ":" + row[1], ((Number) row[2]).longValue()));
        return counts;
    }

    private Map<String, Long> tableCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        PROJECT_TABLES.forEach(table -> counts.put(table, count("SELECT count(*) FROM " + table)));
        return counts;
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @TestConfiguration
    static class CheapPasswordEncoderConfig {
        @Bean
        @Primary
        PasswordEncoder cheapPasswordEncoder() {
            return new DelegatingPasswordEncoder("bcrypt", Map.of("bcrypt", new BCryptPasswordEncoder(4)));
        }
    }
}
