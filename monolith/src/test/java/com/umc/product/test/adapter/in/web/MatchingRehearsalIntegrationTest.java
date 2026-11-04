package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
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

    @Autowired private GetMemberUseCase getMemberUseCase;
    @Autowired private UpdatePartQuotasUseCase updatePartQuotasUseCase;
    @Autowired private PublishProjectUseCase publishProjectUseCase;
    @Autowired private CreateProjectMatchingRoundUseCase createProjectMatchingRoundUseCase;
    @Autowired private TransactionTemplate transactionTemplate;

    private static final String QUOKKA = "쿼카";
    private static final List<String> PROJECT_TABLES = List.of(
        "project", "project_part_quota", "project_application_form", "form", "question", "project_member",
        "project_application", "form_response", "answer", "answer_choice");

    @Test
    @DisplayName("빈 DB에서 QA 계정, 출발선, 쿼카 1차 개발 차수의 봇 지원·결정을 차례로 만들고, 각 단계를 다시 부르면 아무것도 바꾸지 않는다")
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
            .isEqualTo("[\"hansung_g11_qa13@university.neordinary.com\","
                + "\"hansung_g11_qa26@university.neordinary.com\"]");
        Map<String, Long> afterLine = tableCounts();

        // when
        JsonNode again = post("/test/seed/projects/starting-line");

        // then
        assertThat(again.path("unchanged").asInt()).as(again.toPrettyString()).isEqualTo(59);
        assertThat(tableCounts()).isEqualTo(afterLine);

        // given
        Long superadmin = memberId("superadmin@university.neordinary.com");
        publishQaProjects(QUOKKA, superadmin);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Long roundId = createProjectMatchingRoundUseCase.create(CreateProjectMatchingRoundCommand.builder()
            .requesterMemberId(superadmin).name("쿼카 1차 개발").type(MatchingType.PLAN_DEVELOPER)
            .phase(MatchingPhase.FIRST).chapterId(chapterId(QUOKKA)).startsAt(start)
            .endsAt(start.plus(1, ChronoUnit.DAYS)).decisionDeadline(start.plus(2, ChronoUnit.DAYS))
            .resultPublishAt(start.plus(3, ChronoUnit.DAYS)).build());

        // when
        JsonNode summary = get("/test/seed/matching/rounds/" + roundId + "/summary");

        // then
        assertThat(summary.path("round").path("stage").asText()).isEqualTo("BEFORE_START");
        assertThat(summary.path("projects")).hasSize(15 + 2);
        assertThat(summary.path("projects").findValuesAsText("qaProject")).filteredOn("true"::equals).hasSize(2);
        assertThat(summary.path("parts").findValuesAsText("eligible")).containsExactly("61", "34");
        assertThat(summary.path("parts").findValuesAsText("applied")).containsOnly("0");
        assertThat(summary.path("qaReserved")).hasSize(6);
        assertThat(summary.path("projectsWithoutApplicants")).hasSize(17);
        assertThat(postStatus("/test/seed/matching/rounds/" + roundId + "/bot-applications")).isEqualTo(409);

        // given
        moveRound(roundId, "starts_at = now() - interval '1 minute'");

        // when
        JsonNode applied = post("/test/seed/matching/rounds/" + roundId + "/bot-applications");

        // then
        int pool = 61 + 34 - 6;
        int expectedApplicants = Math.round(0.8f * pool);
        assertThat(applied.path("failed").size()).as(applied.path("failed").toString()).isZero();
        assertThat(applied.path("outcome").asText()).isEqualTo("APPLIED");
        assertThat(applied.path("processed").asInt()).isEqualTo(expectedApplicants);
        JsonNode afterApply = applied.path("summary");
        assertThat(sum(afterApply.path("parts").findValuesAsText("applied"))).isEqualTo(expectedApplicants);
        assertThat(sum(afterApply.path("projects").findValuesAsText("appliedBot"))).isEqualTo(expectedApplicants);
        assertThat(afterApply.path("qaReserved").findValuesAsText("applicationStatus")).containsOnly("null");
        assertThat(count("""
            SELECT count(*) FROM project_application WHERE applied_matching_round_id = %d AND status = 'SUBMITTED'
            """.formatted(roundId))).isEqualTo(expectedApplicants);
        Map<String, Long> afterBotApply = tableCounts();

        // when
        JsonNode applyAgain = post("/test/seed/matching/rounds/" + roundId + "/bot-applications");

        // then
        assertThat(applyAgain.path("outcome").asText()).as(applyAgain.toPrettyString()).isEqualTo("UNCHANGED");
        assertThat(applyAgain.path("skipped").path("ALREADY_APPLIED").asInt()).isEqualTo(expectedApplicants);
        assertThat(tableCounts()).isEqualTo(afterBotApply);
        assertThat(postStatus("/test/seed/matching/rounds/" + roundId + "/bot-decisions")).isEqualTo(409);

        // given
        moveRound(roundId, "ends_at = now() - interval '1 second'");
        assertThat(postStatus("/test/seed/matching/rounds/" + roundId + "/bot-applications")).isEqualTo(409);

        // when
        JsonNode decided = post("/test/seed/matching/rounds/" + roundId + "/bot-decisions");

        // then
        assertThat(decided.path("failed").size()).as(decided.path("failed").toString()).isZero();
        assertThat(decided.path("outcome").asText()).isEqualTo("APPLIED");
        assertThat(decided.path("skipped").path("QA_PROJECT").asInt()).isEqualTo(2);
        JsonNode afterDecide = decided.path("summary");
        assertThat(afterDecide.path("round").path("stage").asText()).isEqualTo("DECISION_WINDOW");
        int approved = 0;
        for (JsonNode project : afterDecide.path("projects")) {
            for (JsonNode part : project.path("parts")) {
                int decidedCount = part.path("approved").asInt() + part.path("rejected").asInt();
                int appliedCount = part.path("appliedBot").asInt() + part.path("appliedQa").asInt();
                assertThat(part.path("approved").asInt()).isLessThanOrEqualTo(part.path("quota").asInt());
                assertThat(decidedCount + part.path("undecided").asInt()).isEqualTo(appliedCount);
                if (project.path("qaProject").asBoolean()) {
                    assertThat(decidedCount).as("QA 프로젝트는 봇이 결정하지 않는다").isZero();
                }
                approved += part.path("approved").asInt();
            }
        }
        assertThat(approved).isPositive();
        assertThat(decided.path("processed").asInt()).isEqualTo(approved + sum(
            afterDecide.path("projects").findValuesAsText("rejected")));
        Map<String, Long> afterBotDecide = tableCounts();

        // when
        JsonNode decideAgain = post("/test/seed/matching/rounds/" + roundId + "/bot-decisions");

        // then
        assertThat(decideAgain.path("outcome").asText()).as(decideAgain.toPrettyString()).isEqualTo("UNCHANGED");
        assertThat(decideAgain.path("summary").path("projects")).isEqualTo(afterDecide.path("projects"));
        assertThat(tableCounts()).isEqualTo(afterBotDecide);
    }

    private void moveRound(Long roundId, String assignment) {
        transactionTemplate.executeWithoutResult(status -> entityManager
            .createNativeQuery("UPDATE project_matching_round SET " + assignment + " WHERE id = " + roundId)
            .executeUpdate());
    }

    private int postStatus(String path) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(path)).andReturn().getResponse().getStatus();
    }

    private static int sum(List<String> values) {
        return values.stream().mapToInt(Integer::parseInt).sum();
    }

    private void publishQaProjects(String chapter, Long superadmin) {
        List<Long> projectIds = ids("""
            SELECT p.id FROM project p JOIN chapter c ON c.id = p.chapter_id
            WHERE c.name = '%s' AND p.status = 'PENDING_REVIEW' ORDER BY p.id
            """.formatted(chapter));
        List<ChallengerPart> parts = List.of(
            ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        for (int index = 0; index < projectIds.size(); index++) {
            Long projectId = projectIds.get(index);
            updatePartQuotasUseCase.update(UpdatePartQuotasCommand.builder().projectId(projectId)
                .requesterMemberId(superadmin)
                .entries(List.of(UpdatePartQuotasCommand.Entry.builder().part(parts.get(index)).quota(3L).build()))
                .build());
            publishProjectUseCase.publish(PublishProjectCommand.builder().projectId(projectId)
                .requesterMemberId(superadmin).build());
        }
    }

    private JsonNode get(String path) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(path))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("result");
    }

    private Long memberId(String email) {
        return getMemberUseCase.findIdsByEmails(Set.of(email)).get(email);
    }

    private Long chapterId(String chapter) {
        return ids("SELECT id FROM chapter WHERE name = '%s'".formatted(chapter)).getFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Long> ids(String sql) {
        List<Object> rows = entityManager.createNativeQuery(sql).getResultList();
        return rows.stream().map(row -> ((Number) row).longValue()).toList();
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
            SELECT c.name, p.status, count(*) FROM project p JOIN chapter c ON c.id = p.chapter_id
            GROUP BY c.name, p.status
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
