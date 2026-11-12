package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.ProjectQuotaManagementFixture;

@AutoConfigureMockMvc(addFilters = false)
@Import(ProjectQuotaEditExtensionIntegrationTest.FixedTimeConfiguration.class)
@TestPropertySource(properties = "app.project.quota-edit.open-until=2026-11-13T04:00:00Z")
class ProjectQuotaEditExtensionIntegrationTest extends IntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    @Autowired ProjectQuotaManagementFixture fixture;
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;
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
    void 연장_기간에는_첫_매칭_시작_후에도_TO를_수정한다() throws Exception {
        // given
        fixture.차수(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST,
            NOW.minusSeconds(7200), NOW.minusSeconds(6000), NOW.minusSeconds(5000));

        // when & then
        save(List.of(change(scenario.projectId(), "DESIGN", 2))).andExpect(status().isOk());
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(2);
    }

    @Test
    void 자동선발_전_차수의_PM_합격_인원보다_TO를_줄이거나_없앨_수_없다() throws Exception {
        // given
        Long second = projects.회원(scenario, "디자인 합격 둘", ChallengerPart.DESIGN);
        fixture.정원(scenario.projectId(), ChallengerPart.DESIGN, 3);
        projects.PM_합격_처리(scenario, MatchingType.PLAN_DESIGN, NOW, List.of(scenario.candidateId(), second));

        // when & then
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 1))), "PROJECT-0252");
        denied(save(List.of(change(scenario.projectId(), "DESIGN", 0))), "PROJECT-0252");
        denied(legacyPut(List.of(Map.of("part", "DESIGN", "quota", 1))), "PROJECT-0252");
        denied(legacyPut(List.of(Map.of("part", "WEB_PRODUCT_ENGINEER", "quota", 1))), "PROJECT-0252");
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(3);
        save(List.of(change(scenario.projectId(), "DESIGN", 2))).andExpect(status().isOk());
        assertThat(quota(scenario.projectId(), "DESIGN")).isEqualTo(2);
    }

    @Test
    void PM_합격자가_있는_개발_파트는_다른_개발_파트로_전환할_수_없다() throws Exception {
        // given
        Long web = projects.회원(scenario, "웹 합격", ChallengerPart.WEB_PRODUCT_ENGINEER);
        fixture.정원(scenario.projectId(), ChallengerPart.WEB_PRODUCT_ENGINEER, 1);
        projects.PM_합격_처리(scenario, MatchingType.PLAN_DEVELOPER, NOW, List.of(web));

        // when & then
        denied(save(List.of(Map.of("projectId", scenario.projectId(), "entries", List.of(
            Map.of("part", "WEB_PRODUCT_ENGINEER", "quota", 0),
            Map.of("part", "MOBILE_PRODUCT_ENGINEER", "quota", 1))))), "PROJECT-0252");
        assertThat(quota(scenario.projectId(), "WEB_PRODUCT_ENGINEER")).isEqualTo(1);
        assertThat(quota(scenario.projectId(), "MOBILE_PRODUCT_ENGINEER")).isZero();
    }

    private Map<String, Object> change(Long projectId, String part, long quota) {
        return Map.of("projectId", projectId, "entries", List.of(Map.of("part", part, "quota", quota)));
    }

    private ResultActions save(List<Map<String, Object>> changes) throws Exception {
        return mockMvc.perform(patch("/api/v1/projects/admin/part-quotas").contentType(MediaType.APPLICATION_JSON)
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
