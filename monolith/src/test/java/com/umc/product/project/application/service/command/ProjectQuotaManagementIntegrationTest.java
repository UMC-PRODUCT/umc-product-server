package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.SaveProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.command.dto.SaveProjectQuotaManagementCommand;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.fixture.ProjectManagementPermissionFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.ProjectQuotaManagementFixture;

@AutoConfigureMockMvc(addFilters = false)
@Import(ProjectQuotaManagementIntegrationTest.FixedTimeConfiguration.class)
class ProjectQuotaManagementIntegrationTest extends IntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    private static final String PATH = "/api/v1/projects/admin/part-quotas";
    @Autowired ProjectQuotaManagementFixture fixture;
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectManagementPermissionFixture permissionFixture;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;
    @Autowired SaveProjectQuotaManagementUseCase saveProjectQuotaManagementUseCase;
    @Autowired PlatformTransactionManager transactionManager;
    private Scenario scenario;

    @TestConfiguration
    static class FixedTimeConfiguration {
        @Bean
        @Primary
        Clock quotaTestClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void 지부_프로젝트와_파트별_인원을_준비한다() {
        scenario = fixture.기본();
        authenticate(scenario.staffId());
    }

    @AfterEach
    void 인증과_권한_캐시를_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void 첫_조회로_선택지_표_인원_합계_편집가능_여부를_받는다() throws Exception {
        // when
        JsonNode info = view(null);

        // then
        assertThat(info.path("chapterId").asLong()).isEqualTo(scenario.chapterId());
        assertThat(info.path("chapters").size()).isEqualTo(1);
        assertThat(info.path("schools").get(0).path("schoolId").asLong()).isEqualTo(scenario.schoolId());
        assertThat(info.path("columns").size()).isEqualTo(4);
        assertThat(cell(info, "PLAN").path("quota").asLong()).isEqualTo(1);
        assertThat(cell(info, "PLAN").path("canEdit").path("allowed").asBoolean()).isTrue();
        JsonNode design = part(info.path("summary"), "DESIGN");
        assertThat(design.path("availableCount").asLong()).isEqualTo(2);
        assertThat(design.path("allocatedQuota").asLong()).isEqualTo(1);
        assertThat(design.path("allocationStatus").asText()).isEqualTo("SHORTAGE");
    }

    @Test
    void 검색결과가_없어도_전체_지부_합계는_유지하고_필터_합계는_별도로_반환한다() throws Exception {
        // when
        JsonNode info = view("존재하지않는프로젝트");

        // then
        assertThat(info.path("projects").size()).isZero();
        assertThat(info.path("totalProjectCount").asInt()).isEqualTo(1);
        assertThat(info.path("filteredProjectCount").asInt()).isZero();
        assertThat(part(info.path("summary"), "DESIGN").path("allocatedQuota").asLong()).isEqualTo(1);
        assertThat(part(info.path("filteredSummary"), "DESIGN").path("allocatedQuota").asLong()).isZero();
    }

    @Test
    void 학교_필터는_해당_지부의_학교만_선택할_수_있다() throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);

        // when & then
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
                .param("schoolId", scenario.schoolId().toString())).andExpect(status().isOk());
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
                .param("schoolId", foreign.schoolId().toString())).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT",
        "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN", "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER"})
    void 학교운영진이나_중앙일반운영진이_PM을_겸해도_TO_관리에는_접근할_수_없다(ChallengerRoleType role) throws Exception {
        // given
        Long actor = permissionFixture.역할_회원(scenario, role, ChallengerPart.PLAN);
        authenticate(actor);

        // when & then
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())).andExpect(status().isForbidden());
        save(List.of(change(scenario.projectId(), "DESIGN", 2))).andExpect(status().isForbidden());
    }

    @Test
    void PM_본인도_TO_관리_화면과_저장에는_접근할_수_없다() throws Exception {
        // given
        authenticate(scenario.ownerId());

        // when & then
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())).andExpect(status().isForbidden());
        save(List.of(change(scenario.projectId(), "DESIGN", 2))).andExpect(status().isForbidden());
    }

    @Test
    void 지부장은_소속_범위만_관리하고_총괄단과_최고관리자는_정해진_범위를_관리한다() throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);
        Scenario otherGisu = projects.다른_기수();

        // when & then
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
            .param("chapterId", foreign.chapterId().toString())).andExpect(status().isForbidden());
        projects.총괄단(scenario);
        authenticate(scenario.staffId());
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
            .param("chapterId", foreign.chapterId().toString())).andExpect(status().isOk());
        mockMvc.perform(get(PATH).param("gisuId", otherGisu.gisuId().toString())).andExpect(status().isForbidden());
        permissionFixture.최고관리자(scenario.staffId());
        authenticate(scenario.staffId());
        mockMvc.perform(get(PATH).param("gisuId", otherGisu.gisuId().toString())).andExpect(status().isOk());
    }

    @Test
    void 부족은_허용하고_변경한_셀만_저장하며_0은_미모집으로_처리한다() throws Exception {
        // when
        save(List.of(change(scenario.projectId(), "WEB_PRODUCT_ENGINEER", 1))).andExpect(status().isOk());

        // then
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
        assertThat(quota(scenario.projectId(), "WEB_PRODUCT_ENGINEER")).isEqualTo(1);
        save(List.of(change(scenario.projectId(), "WEB_PRODUCT_ENGINEER", 0))).andExpect(status().isOk());
        assertThat(quota(scenario.projectId(), "WEB_PRODUCT_ENGINEER")).isZero();
        assertThat(cell(view(null), "WEB_PRODUCT_ENGINEER").path("quota").asLong()).isZero();
    }

    @Test
    void PM_TO는_현재_인원과_구분해_저장하고_공동PM보다_낮출_수_없다() throws Exception {
        // given
        save(List.of(change(scenario.projectId(), "PLAN", 2))).andExpect(status().isOk());
        fixture.배정(scenario, scenario.coPmId(), ChallengerPart.PLAN, false);

        // when & then
        JsonNode plan = cell(view(null), "PLAN");
        assertThat(plan.path("quota").asLong()).isEqualTo(2);
        assertThat(plan.path("currentCount").asLong()).isEqualTo(2);
        assertThat(plan.path("minimumQuota").asLong()).isEqualTo(2);
        denied(save(List.of(change(scenario.projectId(), "PLAN", 1))), "PROJECT-0252");
        assertThat(quota(scenario.projectId(), "PLAN")).isEqualTo(2);
    }

    @Test
    void 인원_초과가_있으면_여러_프로젝트의_변경을_전부_롤백한다() throws Exception {
        // given
        Long other = fixture.프로젝트(scenario, "다른 프로젝트");

        // when & then
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 2), change(other, "DESIGN", 1))), "PROJECT-0251");
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
        assertThat(quota(other, "DESIGN")).isZero();
    }

    @Test
    void 프로젝트_사이의_TO_이동은_최종_합계로_검증한다() throws Exception {
        // given
        Long other = fixture.프로젝트(scenario, "다른 프로젝트");
        fixture.정원(other, ChallengerPart.DESIGN, 1);

        // when & then
        save(List.of(change(scenario.projectId(), "DESIGN", 2), change(other, "DESIGN", 0)))
            .andExpect(status().isOk());
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(2);
        assertThat(quota(other, "DESIGN")).isZero();
    }

    @Test
    void 발표대기_배정_인원보다_줄이거나_기존_PUT로_삭제할_수_없다() throws Exception {
        // given
        fixture.배정(scenario, scenario.candidateId(), ChallengerPart.DESIGN, true);

        // when & then
        assertThat(cell(view(null), "DESIGN").path("minimumQuota").asLong()).isEqualTo(1);
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 0))), "PROJECT-0252");
        denied(legacyPut(List.of(Map.of("part", "WEB_PRODUCT_ENGINEER", "quota", 1))), "PROJECT-0252");
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
    }

    @Test
    void 디자인만_잠기면_개발_TO는_수정하고_잠긴_파트의_동일값은_그대로_유지한다() throws Exception {
        // given
        schedule(MatchingType.PLAN_DESIGN, -7200, 3600);

        // when & then
        var info = view(null);
        assertThat(cell(info, "DESIGN").path("canEdit").path("allowed").asBoolean()).isFalse();
        assertThat(cell(info, "PLAN").path("canEdit").path("allowed").asBoolean()).isFalse();
        assertThat(cell(info, "WEB_PRODUCT_ENGINEER").path("canEdit").path("allowed").asBoolean()).isTrue();
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 2))), "PROJECT-0250");
        save(List.of(Map.of("projectId", scenario.projectId(), "entries", List.of(
            Map.of("part", "PLAN", "quota", 1), Map.of("part", "WEB_PRODUCT_ENGINEER", "quota", 1)))))
            .andExpect(status().isOk());
        legacyPut(List.of(Map.of("part", "PLAN", "quota", 1), Map.of("part", "DESIGN", "quota", 1),
            Map.of("part", "WEB_PRODUCT_ENGINEER", "quota", 2))).andExpect(status().isOk());
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
        assertThat(quota(scenario.projectId(), "WEB_PRODUCT_ENGINEER")).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({"-1,true", "0,false", "1,false"})
    void 삼차_판정_마감_후에는_발표를_기다리지_않고_수정을_재개한다(long deadlineOffset, boolean allowed) throws Exception {
        // given
        schedule(MatchingType.PLAN_DESIGN, -7200, deadlineOffset);

        // when & then
        boolean capability = cell(view(null), "DESIGN").path("canEdit").path("allowed").asBoolean();
        assertThat(capability).isEqualTo(allowed);
        save(List.of(change(scenario.projectId(), "DESIGN", 2)))
            .andExpect(status().is(allowed ? 200 : 409));
    }

    @ParameterizedTest
    @ValueSource(strings = {"지부장", "총괄단", "최고관리자"})
    void 상위_운영진도_잠긴_파트를_수정할_수_없다(String role) throws Exception {
        // given
        if (role.equals("총괄단")) {
            projects.총괄단(scenario);
        } else if (role.equals("최고관리자")) {
            permissionFixture.최고관리자(scenario.staffId());
        }
        authenticate(scenario.staffId());
        schedule(MatchingType.PLAN_DESIGN, -7200, 3600);

        // when & then
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 2))), "PROJECT-0250");
    }

    @Test
    void 시작후_3차_일정이_없으면_잠금_사유와_미정인_재개시각을_내려준다() throws Exception {
        // given
        fixture.차수(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST,
            NOW.minusSeconds(7200), NOW.minusSeconds(6000), NOW.minusSeconds(5000));

        // when & then
        JsonNode column = part(view(null).path("columns"), "DESIGN");
        assertThat(column.path("canEdit").path("reasonCode").asText())
            .isEqualTo("PART_QUOTA_MATCHING_SCHEDULE_INCOMPLETE");
        assertThat(column.path("editableAfter").isNull()).isTrue();
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 2))), "PROJECT-0250");
    }

    @Test
    void 다른_지부의_프로젝트를_섞으면_앞선_변경도_롤백한다() throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);

        // when & then
        save(List.of(change(scenario.projectId(), "DESIGN", 2), change(foreign.projectId(), "DESIGN", 0)))
            .andExpect(status().isForbidden());
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
    }

    @Test
    void 중복_프로젝트와_중복_파트_음수_입력을_거부한다() throws Exception {
        // when & then
        save(List.of(change(scenario.projectId(), "DESIGN", 2), change(scenario.projectId(), "DESIGN", 1)))
            .andExpect(status().isBadRequest());
        Map<String, Object> duplicate = Map.of("projectId", scenario.projectId(), "entries",
            List.of(Map.of("part", "DESIGN", "quota", 1), Map.of("part", "DESIGN", "quota", 2)));
        save(List.of(duplicate)).andExpect(status().isBadRequest());
        save(List.of(change(scenario.projectId(), "DESIGN", -1))).andExpect(status().isBadRequest());
    }

    @Test
    void 디자인과_개발이_모두_잠기면_기존_capability도_수정불가로_반환한다() throws Exception {
        // given
        schedule(MatchingType.PLAN_DESIGN, -7200, 3600);
        schedule(MatchingType.PLAN_DEVELOPER, -7200, 3600);

        // when
        String body = mockMvc.perform(get("/api/v1/projects/permissions")
                .param("ids", scenario.projectId().toString())).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // then
        assertThat(objectMapper.readTree(body).path("result").path("projects").get(0)
            .path("partQuota").path("canEdit").path("allowed").asBoolean()).isFalse();
    }

    @Test
    void 지부_이동을_인원_현황과_상한에_반영한다() throws Exception {
        // given
        Scenario destination = projects.다른_지부(scenario);
        projects.매칭지부_이동(scenario, destination);

        // when & then
        assertThat(part(view(null).path("summary"), "DESIGN").path("availableCount").asLong()).isEqualTo(1);
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 2))), "PROJECT-0251");
        authenticate(destination.staffId());
        JsonNode result = objectMapper.readTree(mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
            .param("chapterId", destination.chapterId().toString())).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString()).path("result");
        assertThat(part(result.path("summary"), "DESIGN").path("availableCount").asLong()).isEqualTo(2);
    }

    @Test
    void 동시_저장도_지부의_파트_인원을_초과할_수_없다() throws Exception {
        // given
        fixture.정원(scenario.projectId(), ChallengerPart.DESIGN, 0);
        Long other = fixture.프로젝트(scenario, "동시 수정");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        // when
        try (var race = new PostgreSqlTransactionRace(transactionManager, jdbc)) {
            var first = race.submit(() -> raceSave(race, ready, start, scenario.projectId()));
            var second = race.submit(() -> raceSave(race, ready, start, other));
            race.await(ready, "두 TO 저장 준비");
            start.countDown();
            boolean one = succeeded(first.future());
            boolean two = succeeded(second.future());

            // then
            assertThat(one ^ two).isTrue();
            assertThat(quota(scenario.projectId(), "DESIGN") + quota(other, "DESIGN")).isEqualTo(2);
        }
    }

    private boolean raceSave(PostgreSqlTransactionRace race, CountDownLatch ready, CountDownLatch start, Long id) {
        ready.countDown();
        race.await(start, "동시 TO 저장 시작");
        saveProjectQuotaManagementUseCase.save(new SaveProjectQuotaManagementCommand(scenario.staffId(),
            scenario.gisuId(), scenario.chapterId(), List.of(new SaveProjectQuotaManagementCommand.ProjectChange(id,
                List.of(new SaveProjectQuotaManagementCommand.CellChange(ChallengerPart.DESIGN, 2L))))));
        return true;
    }

    private boolean succeeded(Future<Boolean> future) throws Exception {
        try {
            return future.get(20, TimeUnit.SECONDS);
        } catch (ExecutionException exception) {
            assertThat(exception.getCause()).isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_CAPACITY_EXCEEDED);
            return false;
        }
    }

    @Test
    void null_프로젝트와_null_셀은_검증오류로_거부한다() throws Exception {
        // when & then
        save(java.util.Collections.singletonList(null)).andExpect(status().isBadRequest());
        Map<String, Object> invalid = Map.of("projectId", scenario.projectId(), "entries",
            java.util.Collections.singletonList(null));
        save(List.of(invalid)).andExpect(status().isBadRequest());
    }

    @Test
    void 합계가_Long_범위를_넘어도_입력오류로_거부하고_롤백한다() throws Exception {
        // given
        Long other = fixture.프로젝트(scenario, "다른 프로젝트");

        // when & then
        denied(save(List.of(change(scenario.projectId(), "DESIGN", Long.MAX_VALUE),
            change(other, "DESIGN", Long.MAX_VALUE))), "PROJECT-0251");
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(1);
        assertThat(quota(other, "DESIGN")).isZero();
    }

    private void schedule(MatchingType type, long firstStart, long thirdDeadline) {
        fixture.차수(scenario, type, MatchingPhase.FIRST, NOW.plusSeconds(firstStart),
            NOW.plusSeconds(firstStart + 600), NOW.plusSeconds(firstStart + 1200));
        fixture.차수(scenario, type, MatchingPhase.THIRD, NOW.plusSeconds(thirdDeadline - 1800),
            NOW.plusSeconds(thirdDeadline - 600), NOW.plusSeconds(thirdDeadline));
    }

    private JsonNode view(String keyword) throws Exception {
        var request = get(PATH).param("gisuId", scenario.gisuId().toString());
        if (keyword != null) {
            request.param("keyword", keyword);
        }
        return objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString()).path("result");
    }

    private JsonNode cell(JsonNode info, String part) {
        return part(info.path("projects").get(0).path("cells"), part);
    }

    private JsonNode part(JsonNode nodes, String part) {
        for (JsonNode node : nodes) {
            if (node.path("part").asText().equals(part)) {
                return node;
            }
        }
        throw new AssertionError("파트 없음: " + part);
    }

    private Map<String, Object> change(Long projectId, String part, long quota) {
        return Map.of("projectId", projectId, "entries", List.of(Map.of("part", part, "quota", quota)));
    }

    private ResultActions save(List<Map<String, Object>> changes) throws Exception {
        return mockMvc.perform(patch(PATH).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of("gisuId", scenario.gisuId(),
                "chapterId", scenario.chapterId(), "projects", changes))));
    }

    private ResultActions legacyPut(List<Map<String, Object>> entries) throws Exception {
        return mockMvc.perform(put("/api/v1/projects/{id}/part-quotas", scenario.projectId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of("entries", entries))));
    }

    private void denied(ResultActions actions, String code) throws Exception {
        String body = actions.andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();
        assertThat(body).contains(code);
    }

    private long quota(Long projectId, String part) {
        Long result = jdbc.queryForObject(
            "SELECT coalesce(sum(quota),0) FROM project_part_quota WHERE project_id=? AND part=?",
            Long.class, projectId, part);
        return result == null ? 0 : result;
    }

    private void authenticate(Long memberId) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        var principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
