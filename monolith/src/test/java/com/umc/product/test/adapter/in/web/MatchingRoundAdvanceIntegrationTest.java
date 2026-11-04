package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.project.adapter.out.scheduler.MatchingRoundDeadlineScheduler;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;

@TestPropertySource(properties = "app.seed.enabled=true")
class MatchingRoundAdvanceIntegrationTest extends IntegrationTestSupport {

    private static final Duration DEADLINE_BUFFER = Duration.ofMinutes(1);

    @Autowired private GisuFixture gisuFixture;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;
    @Autowired private LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Autowired private MatchingRoundDeadlineScheduler scheduler;

    private final List<Long> roundIds = new ArrayList<>();
    private Long gisuId;
    private Long first;
    private Long second;
    private Long random;

    @BeforeEach
    void registerRounds() {
        Gisu gisu = gisuFixture.비활성_기수(95L);
        Chapter chapter = chapterFixture.지부(gisu, "넘기기 지부");
        gisuId = gisu.getId();
        Instant base = Instant.now().plus(Duration.ofDays(3));
        first = register(chapter, MatchingPhase.FIRST, base);
        second = register(chapter, MatchingPhase.SECOND, base.plus(Duration.ofDays(2)));
        register(chapter, MatchingPhase.THIRD, base.plus(Duration.ofDays(4)));
        random = register(chapter, MatchingPhase.RANDOM_MATCHING, base.plus(Duration.ofDays(6)));
    }

    @AfterEach
    void cancelTimers() {
        roundIds.forEach(scheduler::cancel);
    }

    @Test
    @DisplayName("단계를 한 칸씩 넘기면 DB 시각을 지금으로 당기고 새 시각으로 알람을 다시 맞추며, 순서가 어긋나면 409로 거부한다")
    void advancesStepByStepAndRefusesOutOfOrderSteps() throws Exception {
        advance(first, "APPLICATION_CLOSE").andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SEED-0303"));
        advance(second, "APPLICATION_START").andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SEED-0301"));
        advance(random, "ADJUSTMENT_START").andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SEED-0304"));
        advance(999_999L, "APPLICATION_START").andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SEED-0307"));
        mockMvc.perform(post("/test/seed/matching/rounds/{roundId}/advance", first)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());

        Instant beforeStart = Instant.now();
        JsonNode started = advanceOk(first, "APPLICATION_START");
        ProjectMatchingRound round = round(first);
        assertThat(started.path("outcome").asText()).isEqualTo("APPLIED");
        assertThat(round.getStartsAt()).isBetween(beforeStart, Instant.now());
        assertThat(round.isOpenAt(Instant.now())).isTrue();
        assertThat(round.getEndsAt()).isAfter(Instant.now().plus(Duration.ofHours(23)));
        assertThat(started.path("timers")).hasSize(4);
        assertThat(started.path("summary").path("round").path("roundId").asLong()).isEqualTo(first);
        assertThat(started.path("summary").path("round").path("stage").asText()).isEqualTo("APPLICATION_OPEN");
        assertTimer(started, first, round.getDecisionDeadline().plus(DEADLINE_BUFFER), round.getResultPublishAt());
        assertThat(scheduler.isScheduled(first)).isTrue();

        JsonNode again = advanceOk(first, "APPLICATION_START");
        assertThat(again.path("outcome").asText()).isEqualTo("ALREADY_REACHED");
        assertThat(again.path("timeChanges")).isEmpty();
        assertThat(round(first).getStartsAt()).isEqualTo(round.getStartsAt());
        assertThat(scheduler.isScheduled(first)).isTrue();

        Instant beforeClose = Instant.now();
        advanceOk(first, "APPLICATION_CLOSE");
        round = round(first);
        assertThat(round.getEndsAt()).isBetween(beforeClose, Instant.now());
        assertThat(round.isDecisionDeadlinePassed(Instant.now())).isFalse();

        Instant beforeDeadline = Instant.now();
        JsonNode deadline = advanceOk(first, "DECISION_DEADLINE");
        round = round(first);
        assertThat(round.getDecisionDeadline()).isBetween(beforeDeadline, Instant.now());
        assertThat(round.getResultPublishAt()).isAfter(Instant.now().plus(Duration.ofHours(23)));
        assertTimer(deadline, first, round.getDecisionDeadline().plus(DEADLINE_BUFFER), round.getResultPublishAt());

        advance(first, "RESULT_PUBLISH").andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SEED-0302"));
    }

    @Test
    @DisplayName("선발 마감으로 넘기면 실제 스케줄러가 1분 뒤 자동 선발하고, 발표로 넘기면 결과를 공개해 다음 차수를 시작할 수 있다")
    void realSchedulerAutoDecidesAndPublishesAfterAdvance() throws Exception {
        // given
        advanceOk(first, "APPLICATION_START");
        advanceOk(first, "APPLICATION_CLOSE");

        // when
        advanceOk(first, "DECISION_DEADLINE");

        // then
        await().atMost(Duration.ofSeconds(90)).pollInterval(Duration.ofSeconds(1))
            .until(() -> round(first).isAutoDecisionExecuted());

        JsonNode publish = advanceOk(first, "RESULT_PUBLISH");
        ProjectMatchingRound round = round(first);
        assertThat(round.getResultPublishAt()).isAfterOrEqualTo(
            round.getDecisionDeadline().plus(Duration.ofMinutes(5)));
        assertTimer(publish, first, null, round.getResultPublishAt());
        await().atMost(Duration.ofSeconds(10))
            .until(() -> round(first).getResultPublishedAt() != null);

        advanceOk(second, "APPLICATION_START");
        assertThat(round(second).isOpenAt(Instant.now())).isTrue();
    }

    @Test
    @DisplayName("프로젝트 초기화는 지운 차수의 알람도 취소한다")
    void deletingSeedProjectsCancelsTimersOfDeletedRounds() throws Exception {
        // given
        advanceOk(first, "APPLICATION_START");
        assertThat(scheduler.isScheduled(first)).isTrue();

        // when
        mockMvc.perform(delete("/test/seed/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gisuId\": " + gisuId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.deletedProjectMatchingRounds").value(4));

        // then
        assertThat(roundIds).noneMatch(scheduler::isScheduled);
        assertThat(roundIds).allMatch(id -> loadProjectMatchingRoundPort.findById(id).isEmpty());
    }

    private Long register(Chapter chapter, MatchingPhase phase, Instant startsAt) {
        Instant endsAt = startsAt.plus(Duration.ofDays(1));
        Instant decisionDeadline = phase == MatchingPhase.RANDOM_MATCHING ? endsAt : endsAt.plus(Duration.ofHours(2));
        ProjectMatchingRound round = saveProjectMatchingRoundPort.save(ProjectMatchingRound.create(
            phase.name(), null, MatchingType.PLAN_DEVELOPER, phase, chapter.getId(),
            startsAt, endsAt, decisionDeadline, decisionDeadline.plus(Duration.ofHours(1))));
        roundIds.add(round.getId());
        return round.getId();
    }

    private ResultActions advance(Long roundId, String step) throws Exception {
        return mockMvc.perform(post("/test/seed/matching/rounds/{roundId}/advance", roundId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"step\": \"" + step + "\"}"));
    }

    private JsonNode advanceOk(Long roundId, String step) throws Exception {
        String body = advance(roundId, step).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("result");
    }

    private ProjectMatchingRound round(Long roundId) {
        return loadProjectMatchingRoundPort.getById(roundId);
    }

    private static void assertTimer(JsonNode response, Long roundId, Instant autoDecideAt, Instant publishAt) {
        JsonNode timer = null;
        for (JsonNode each : response.path("timers")) {
            if (each.path("roundId").asLong() == roundId) {
                timer = each;
            }
        }
        assertThat(timer).as("timer of %s in %s", roundId, response).isNotNull();
        assertThat(instantOrNull(timer.path("autoDecideAt"))).isEqualTo(autoDecideAt);
        assertThat(instantOrNull(timer.path("publishAt"))).isEqualTo(publishAt);
    }

    private static Instant instantOrNull(JsonNode node) {
        return node.isNull() || node.isMissingNode() ? null : Instant.parse(node.asText());
    }
}
