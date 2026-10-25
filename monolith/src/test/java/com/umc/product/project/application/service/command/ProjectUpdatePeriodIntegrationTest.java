package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ProjectManagementPermissionFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.ProjectUpdateFixture;

@AutoConfigureMockMvc(addFilters = false)
@Import(ProjectUpdatePeriodIntegrationTest.FixedTimeConfiguration.class)
class ProjectUpdatePeriodIntegrationTest extends IntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectManagementPermissionFixture permissionFixture;
    @Autowired ProjectUpdateFixture fixture;
    @Autowired UpdateProjectUseCase updateProjectUseCase;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;

    @TestConfiguration
    static class FixedTimeConfiguration {
        @Bean
        @Primary
        Clock projectUpdateTestClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @AfterEach
    void 인증과_권한_캐시를_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PM", "지부장", "총괄단", "최고관리자"})
    void 지원기간에는_PM과_운영진_모두_수정할_수_없다(String role) throws Exception {
        // given
        Scenario scenario = projects.준비();
        Long actor = scenario.staffId();
        if (role.equals("PM")) {
            actor = scenario.ownerId();
        } else if (role.equals("총괄단")) {
            projects.총괄단(scenario);
        } else if (role.equals("최고관리자")) {
            permissionFixture.최고관리자(actor);
        }
        authenticate(actor);
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60);

        // when & then
        assertBlocked(scenario.projectId());
        assertCapability(scenario.projectId(), false);
        mockMvc.perform(get("/api/v1/projects/permissions").param("ids", scenario.projectId().toString()))
            .andExpect(jsonPath("$.result.projects[0].canTransferOwnership.allowed").value(true));
    }

    @ParameterizedTest
    @MethodSource("지원_차수")
    void 디자인과_개발의_각_지원차수에서_수정을_차단한다(MatchingType type, MatchingPhase phase) throws Exception {
        // given
        Scenario scenario = projects.준비();
        authenticate(scenario.ownerId());
        round(scenario, type, phase, -60, 60);

        // when & then
        assertBlocked(scenario.projectId());
        assertCapability(scenario.projectId(), false);
    }

    static Stream<Arguments> 지원_차수() {
        return Stream.of(MatchingType.PLAN_DESIGN, MatchingType.PLAN_DEVELOPER)
            .flatMap(type -> Stream.of(MatchingPhase.FIRST, MatchingPhase.SECOND, MatchingPhase.THIRD)
                .map(phase -> Arguments.of(type, phase)));
    }

    @ParameterizedTest
    @CsvSource({"1,60,false", "0,60,true", "-60,60,true", "-60,0,true", "-60,-1,false", "-7200,-3600,false"})
    void 시작과_마감_경계에서_API와_화면의_수정가능_여부가_일치한다(long startsIn, long endsIn, boolean blocked)
        throws Exception {
        // given
        Scenario scenario = projects.준비();
        authenticate(scenario.ownerId());
        round(scenario, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, startsIn, endsIn);

        // when & then
        if (blocked) {
            assertBlocked(scenario.projectId());
        } else {
            update(scenario.projectId(), 200);
            assertThat(name(scenario.projectId())).isEqualTo("수정한 이름");
        }
        assertCapability(scenario.projectId(), !blocked);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"DRAFT", "PENDING_REVIEW", "IN_PROGRESS"})
    void 초안과_승인대기도_지원기간에는_수정을_차단한다(ProjectStatus projectStatus) throws Exception {
        // given
        Scenario scenario = projects.준비();
        Long id = permissionFixture.프로젝트(scenario, scenario.ownerId(), projectStatus);
        authenticate(scenario.ownerId());
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND, -60, 60);

        // when & then
        String before = name(id);
        update(id, 409);
        assertThat(name(id)).isEqualTo(before);
        assertCapability(id, false);
    }

    @Test
    void 다른_지부와_기수의_지원기간은_수정을_막지_않는다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherChapter = projects.다른_지부(scenario);
        Scenario otherGisu = projects.다른_기수();
        round(otherChapter, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60);
        round(otherGisu, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -60, 60);
        authenticate(scenario.ownerId());

        // when & then
        update(scenario.projectId(), 200);
        assertCapability(scenario.projectId(), true);
    }

    @Test
    void 차수_사이의_기간에는_수정을_허용한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -7200, -3600);
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND, 60, 120);
        authenticate(scenario.ownerId());

        // when & then
        update(scenario.projectId(), 200);
        assertCapability(scenario.projectId(), true);
    }

    @Test
    void 랜덤매칭은_지원기간이_아니므로_수정을_허용한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.RANDOM_MATCHING, -60, 60);
        authenticate(scenario.ownerId());

        // when & then
        update(scenario.projectId(), 200);
        assertCapability(scenario.projectId(), true);
    }

    @Test
    void API를_거치지_않는_수정도_지원기간에는_차단한다() {
        // given
        Scenario scenario = projects.준비();
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.THIRD, -60, 60);

        // when & then
        assertThatThrownBy(() -> updateProjectUseCase.update(UpdateProjectCommand.builder()
            .projectId(scenario.projectId()).requesterMemberId(scenario.ownerId()).name("우회 수정").build()))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_UPDATE_DURING_APPLICATION_PERIOD);
        assertThat(name(scenario.projectId())).isEqualTo("매칭 프로젝트");
    }

    @Test
    void 지원기간이_아니어도_타인의_프로젝트를_수정할_수_없다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        authenticate(scenario.candidateId());

        // when & then
        update(scenario.projectId(), 403);
        assertCapability(scenario.projectId(), false);
    }

    @Test
    void 지원기간에도_기존_공동PM_관리는_유지한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        round(scenario, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60);
        authenticate(scenario.ownerId());

        // when & then
        mockMvc.perform(post("/api/v1/projects/{id}/members", scenario.projectId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("memberId", scenario.coPmId(), "part", "PLAN"))))
            .andExpect(status().isOk());
    }

    private void round(Scenario scenario, MatchingType type, MatchingPhase phase, long startsIn, long endsIn) {
        fixture.매칭차수(scenario.chapterId(), type, phase, NOW.plusSeconds(startsIn), NOW.plusSeconds(endsIn));
    }

    private void assertBlocked(Long projectId) throws Exception {
        String before = name(projectId);
        update(projectId, 409);
        assertThat(name(projectId)).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT description FROM project WHERE id = ?", String.class, projectId))
            .isNull();
    }

    private void update(Long projectId, int expectedStatus) throws Exception {
        mockMvc.perform(patch("/api/v1/projects/{id}", projectId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("name", "수정한 이름", "description", "수정한 설명"))))
            .andExpect(status().is(expectedStatus));
    }

    private void assertCapability(Long projectId, boolean allowed) throws Exception {
        mockMvc.perform(get("/api/v1/projects/permissions").param("ids", projectId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects[0].canEditInfo.allowed").value(allowed));
    }

    private String name(Long projectId) {
        return jdbc.queryForObject("SELECT name FROM project WHERE id = ?", String.class, projectId);
    }

    private void authenticate(Long memberId) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        MemberPrincipal principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
