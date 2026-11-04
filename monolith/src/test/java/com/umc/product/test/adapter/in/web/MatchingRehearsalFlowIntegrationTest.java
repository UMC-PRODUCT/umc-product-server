package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.adapter.out.scheduler.MatchingRoundDeadlineScheduler;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.query.GetMyMatchingAffiliationUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaTransfers;
import com.umc.product.test.domain.roster.QaTransfers.QaTransfer;

/**
 * 자동 선발은 알람 등록 시각만 확인하고 타이머가 부를 UseCase를 직접 부른다. 실제 스케줄러 대기는
 * {@link MatchingRoundAdvanceIntegrationTest}가 맡는다.
 */
@TestPropertySource(properties = {
    "app.seed.enabled=true",
    "test.context.key=matching-rehearsal-flow"
})
@Import(QaSeedIntegrationTest.CheapPasswordEncoderConfig.class)
class MatchingRehearsalFlowIntegrationTest extends IntegrationTestSupport {

    private static final String QUOKKA = "쿼카";
    private static final Duration DEADLINE_BUFFER = Duration.ofMinutes(1);
    private static final String SUPERADMIN = QaAccountPlan.legacyEmail(QaAccountPlan.SUPERADMIN);

    @Autowired private GetMemberUseCase getMemberUseCase;
    @Autowired private GetGisuUseCase getGisuUseCase;
    @Autowired private GetMyMatchingAffiliationUseCase getMyMatchingAffiliationUseCase;
    @Autowired private UpdatePartQuotasUseCase updatePartQuotasUseCase;
    @Autowired private PublishProjectUseCase publishProjectUseCase;
    @Autowired private UpdateProjectMatchingRoundUseCase updateProjectMatchingRoundUseCase;
    @Autowired private AutoDecideProjectMatchingRoundUseCase autoDecideProjectMatchingRoundUseCase;
    @Autowired private MatchingRoundDeadlineScheduler scheduler;

    @Test
    @DisplayName("빈 DB에서 QA 계정과 출발선을 만들고 쿼카 1차 개발을 발표까지 넘긴 뒤, 프로젝트를 지우고 출발선을 다시 만들어도 같은 모양이고 계정·지부 이동은 남는다")
    void rehearsesFirstRoundAndRebuildsStartingLine() throws Exception {
        // given
        JsonNode qa = ok(post("/test/seed/qa")).path("steps");
        assertThat(qa.findValuesAsText("status")).as(qa.toPrettyString()).doesNotContain("FAILED", "NOT_RUN");
        JsonNode line = ok(post("/test/seed/projects/starting-line"));
        assertThat(line.path("failed").asInt()).as(line.toPrettyString()).isZero();
        Map<String, Long> projectsAfterLine = countByChapterAndStatus();

        Long superadmin = getMemberUseCase.findIdsByEmails(Set.of(SUPERADMIN)).get(SUPERADMIN);
        publishQaProjects(QUOKKA, superadmin);
        List<Long> rounds = registerDeveloperRounds(QUOKKA, superadmin);
        Long first = rounds.get(0);
        Long second = rounds.get(1);

        // when
        JsonNode started = advance(first, "APPLICATION_START");
        JsonNode applied = ok(post("/test/seed/matching/rounds/{roundId}/bot-applications", first));
        advance(first, "APPLICATION_CLOSE");
        JsonNode decided = ok(post("/test/seed/matching/rounds/{roundId}/bot-decisions", first));
        JsonNode deadline = advance(first, "DECISION_DEADLINE");

        // then
        assertThat(started.path("summary").path("round").path("stage").asText()).isEqualTo("APPLICATION_OPEN");
        assertThat(applied.path("processed").asInt()).as(applied.toPrettyString()).isPositive();
        assertThat(applied.path("failed").size()).as(applied.path("failed").toString()).isZero();
        assertThat(decided.path("failed").size()).as(decided.path("failed").toString()).isZero();
        int approvedByBot = sum(decided.path("summary").path("projects").findValuesAsText("approved"));
        assertThat(approvedByBot).isPositive();

        JsonNode afterDeadline = deadline.path("summary").path("round");
        assertThat(afterDeadline.path("stage").asText()).isEqualTo("AWAITING_AUTO_DECISION");
        Instant decisionDeadline = Instant.parse(afterDeadline.path("decisionDeadline").asText());
        assertThat(instant(timer(deadline, first).path("autoDecideAt")))
            .isEqualTo(decisionDeadline.plus(DEADLINE_BUFFER));
        assertThat(scheduler.isScheduled(first)).isTrue();
        advanceRefused(second, "APPLICATION_START", "SEED-0301");
        advanceRefused(first, "RESULT_PUBLISH", "SEED-0302");

        // when
        autoDecideProjectMatchingRoundUseCase.autoDecide(first, null);
        JsonNode publishing = advance(first, "RESULT_PUBLISH");

        // then
        assertThat(timer(publishing, first).path("autoDecideAt").isNull()).isTrue();
        await().atMost(Duration.ofSeconds(10))
            .until(() -> summary(first).path("round").path("published").asBoolean());
        JsonNode published = summary(first);
        assertThat(published.path("round").path("autoDecided").asBoolean()).isTrue();
        assertThat(published.path("round").path("stage").asText()).isEqualTo("PUBLISHED");
        int selected = 0;
        for (JsonNode project : published.path("projects")) {
            for (JsonNode part : project.path("parts")) {
                int quota = part.path("quota").asInt();
                assertThat(part.path("approved").asInt()).as(project.toString()).isLessThanOrEqualTo(quota);
                assertThat(part.path("existingMembers").asInt() + part.path("selected").asInt())
                    .as(project.toString()).isLessThanOrEqualTo(quota);
                selected += part.path("selected").asInt();
            }
        }
        assertThat(selected).as("자동 선발은 봇 합격자를 모두 팀원으로 넣는다").isGreaterThanOrEqualTo(approvedByBot);
        assertThat(sum(published.path("parts").findValuesAsText("withTeam")))
            .as("팀이 생긴 사람 수는 이 차수 선발 수와 같다").isEqualTo(selected);

        JsonNode secondStarted = advance(second, "APPLICATION_START");
        assertThat(secondStarted.path("outcome").asText()).isEqualTo("APPLIED");
        assertThat(secondStarted.path("summary").path("round").path("stage").asText()).isEqualTo("APPLICATION_OPEN");

        // when
        Long gisuId = getGisuUseCase.getActiveGisuId();
        mockMvc.perform(delete("/test/seed/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gisuId\": " + gisuId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.deletedProjectMatchingRounds").value(4));
        JsonNode rebuilt = ok(post("/test/seed/projects/starting-line"));

        // then
        assertThat(rounds).noneMatch(scheduler::isScheduled);
        assertThat(rebuilt.path("failed").asInt()).as(rebuilt.toPrettyString()).isZero();
        assertThat(rebuilt.path("applied").asInt()).isEqualTo(59);
        assertThat(rebuilt.path("chapters")).isEqualTo(line.path("chapters"));
        assertThat(countByChapterAndStatus()).isEqualTo(projectsAfterLine);
        assertThat(getMemberUseCase.countAll()).isEqualTo(593);
        Map<String, Long> memberIds = getMemberUseCase.findIdsByEmails(Set.copyOf(
            QaTransfers.ALL.stream().map(transfer -> transfer.member().email()).toList()));
        for (QaTransfer transfer : QaTransfers.ALL) {
            assertThat(getMyMatchingAffiliationUseCase.get(memberIds.get(transfer.member().email()), gisuId)
                .matchingChapter().name()).isEqualTo(transfer.toChapter());
        }
    }

    private void publishQaProjects(String chapter, Long superadmin) {
        List<Long> projectIds = ids("""
            SELECT p.id FROM project p JOIN chapter c ON c.id = p.chapter_id
            WHERE c.name = '%s' AND p.status = 'PENDING_REVIEW' ORDER BY p.id
            """.formatted(chapter));
        assertThat(projectIds).hasSize(2);
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

    private List<Long> registerDeveloperRounds(String chapter, Long superadmin) {
        Long chapterId = ids("SELECT id FROM chapter WHERE name = '%s'".formatted(chapter)).getFirst();
        Instant base = Instant.now().plus(Duration.ofDays(3));
        List<CreateProjectMatchingRoundCommand> commands = List.of(MatchingPhase.values()).stream()
            .map(phase -> {
                Instant startsAt = base.plus(Duration.ofDays(2L * phase.ordinal()));
                Instant endsAt = startsAt.plus(Duration.ofDays(1));
                Instant decisionDeadline = phase == MatchingPhase.RANDOM_MATCHING
                    ? endsAt : endsAt.plus(Duration.ofHours(2));
                return CreateProjectMatchingRoundCommand.builder().requesterMemberId(superadmin)
                    .name(chapter + " " + phase.name()).type(MatchingType.PLAN_DEVELOPER).phase(phase)
                    .chapterId(chapterId).startsAt(startsAt).endsAt(endsAt).decisionDeadline(decisionDeadline)
                    .resultPublishAt(decisionDeadline.plus(Duration.ofHours(1))).build();
            })
            .toList();
        return updateProjectMatchingRoundUseCase.saveAll(commands).stream()
            .map(ProjectMatchingRoundInfo::id).toList();
    }

    private JsonNode advance(Long roundId, String step) throws Exception {
        return ok(advanceRequest(roundId, step));
    }

    private void advanceRefused(Long roundId, String step, String code) throws Exception {
        mockMvc.perform(advanceRequest(roundId, step))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(code));
    }

    private static RequestBuilder advanceRequest(Long roundId, String step) {
        return post("/test/seed/matching/rounds/{roundId}/advance", roundId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"step\": \"" + step + "\"}");
    }

    private JsonNode summary(Long roundId) throws Exception {
        return ok(get("/test/seed/matching/rounds/{roundId}/summary", roundId));
    }

    private JsonNode ok(RequestBuilder request) throws Exception {
        ResultActions result = mockMvc.perform(request).andExpect(status().isOk());
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString()).path("result");
    }

    private static JsonNode timer(JsonNode advanced, Long roundId) {
        for (JsonNode each : advanced.path("timers")) {
            if (each.path("roundId").asLong() == roundId) {
                return each;
            }
        }
        throw new AssertionError("차수 " + roundId + "의 알람이 응답에 없습니다: " + advanced);
    }

    private static Instant instant(JsonNode node) {
        return node.isNull() || node.isMissingNode() ? null : Instant.parse(node.asText());
    }

    private static int sum(List<String> values) {
        return values.stream().mapToInt(Integer::parseInt).sum();
    }

    @SuppressWarnings("unchecked")
    private List<Long> ids(String sql) {
        List<Object> rows = entityManager.createNativeQuery(sql).getResultList();
        return rows.stream().map(row -> ((Number) row).longValue()).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> countByChapterAndStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT c.name, p.status, count(*) FROM project p JOIN chapter c ON c.id = p.chapter_id
            GROUP BY c.name, p.status ORDER BY c.name, p.status
            """).getResultList();
        rows.forEach(row -> counts.put(row[0] + ":" + row[1], ((Number) row[2]).longValue()));
        return counts;
    }
}
