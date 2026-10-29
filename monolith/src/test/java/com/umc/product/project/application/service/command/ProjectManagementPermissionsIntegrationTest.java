package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ProjectManagementPermissionFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;

@AutoConfigureMockMvc(addFilters = false)
class ProjectManagementPermissionsIntegrationTest extends IntegrationTestSupport {
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectManagementPermissionFixture fixture;
    @Autowired CreateDraftProjectUseCase createDraftProjectUseCase;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void 인증과_권한_캐시를_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class,
        names = {"CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER"})
    void 중앙운영진은_소속_기수_전체_지부의_승인대기와_등록완료만_조회한다(ChallengerRoleType role) throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherChapter = projects.다른_지부(scenario);
        fixture.모든_상태(scenario);
        fixture.모든_상태(otherChapter);
        projects.다른_기수();
        Long actor = fixture.역할_회원(scenario, role, ChallengerPart.DESIGN);
        authenticate(actor);

        // when & then
        List<JsonNode> rows = content(managed(scenario).param("size", "20"), 4);
        assertThat(rows).extracting(row -> row.get("status").asText())
            .containsOnly("PENDING_REVIEW", "IN_PROGRESS");
        assertThat(rows.subList(0, 2)).allSatisfy(row ->
            assertThat(row.get("status").asText()).isEqualTo("IN_PROGRESS"));
        content(managed(scenario).param("keyword", "COMPLETED"), 0);
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT",
        "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN"})
    void 학교_운영진_역할만으로는_관리_목록과_대리_등록에_접근하지_못한다(ChallengerRoleType role) throws Exception {
        // given
        Scenario scenario = projects.준비();
        fixture.모든_상태(scenario);
        Long actor = fixture.역할_회원(scenario, role, ChallengerPart.DESIGN);
        authenticate(actor);
        long before = projectCount();

        // when & then
        content(managed(scenario), 0);
        create(scenario, scenario.ownerId()).andExpect(status().isForbidden());
        assertThat(projectCount()).isEqualTo(before);
        // 관리 목록 권한 변경으로 기존 공개 상세까지 막지 않는다.
        mockMvc.perform(get("/api/v1/projects/{id}", scenario.projectId())).andExpect(status().isOk());
        // 컨트롤러 진입 검사를 거치지 않는 내부 호출에서도 대리 등록을 거부한다.
        assertThatThrownBy(() -> createDraftProjectUseCase.create(CreateDraftProjectCommand.builder()
            .gisuId(scenario.gisuId()).productOwnerMemberId(scenario.ownerId()).requesterMemberId(actor).build()))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT",
        "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN", "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER"})
    void 학교나_중앙운영진이_PM을_겸하면_본인_프로젝트와_등록_권한은_유지한다(ChallengerRoleType role) throws Exception {
        // given
        Scenario scenario = projects.준비();
        var others = fixture.모든_상태(scenario);
        Long actor = fixture.역할_회원(scenario, role, ChallengerPart.PLAN);
        Long completed = fixture.프로젝트(scenario, actor, ProjectStatus.COMPLETED);
        authenticate(actor);

        // when
        String response = create(scenario, actor).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Long draft = objectMapper.readTree(response).path("result").path("projectId").asLong();

        // then
        int total = role.isAtLeastCentralMember() ? 4 : 2;
        Set<Long> ids = ids(content(managed(scenario), total));
        assertThat(ids).contains(draft, completed)
            .doesNotContain(others.get(ProjectStatus.DRAFT), others.get(ProjectStatus.COMPLETED),
                others.get(ProjectStatus.ABORTED));
        // PLAN 자격으로 생성 진입은 가능해도 학교·일반 중앙 역할로 타인을 대리 등록할 수는 없다.
        create(scenario, scenario.ownerId()).andExpect(status().isForbidden());
    }

    @Test
    void 총괄단은_소속_기수_전체_지부를_조회하고_대리_등록한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherChapter = projects.다른_지부(scenario);
        fixture.모든_상태(scenario);
        fixture.모든_상태(otherChapter);
        Long actor = fixture.역할_회원(scenario, ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerPart.DESIGN);
        authenticate(actor);
        // when & then
        content(managed(scenario), 8);
        create(otherChapter, otherChapter.ownerId()).andExpect(status().isOk());
    }

    @Test
    void 지부장은_소속_지부만_조회하고_등록하며_PM은_본인_프로젝트만_조회한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherChapter = projects.다른_지부(scenario);
        var mine = fixture.모든_상태(scenario);
        fixture.모든_상태(otherChapter);
        authenticate(scenario.staffId());
        // when & then
        Set<Long> staffIds = ids(content(managed(scenario), 4));
        assertThat(staffIds).contains(mine.get(ProjectStatus.COMPLETED), mine.get(ProjectStatus.ABORTED))
            .doesNotContain(mine.get(ProjectStatus.DRAFT), otherChapter.projectId());
        create(scenario, scenario.coPmId()).andExpect(status().isOk());
        create(otherChapter, otherChapter.ownerId()).andExpect(status().isForbidden());
        authenticate(scenario.ownerId());
        assertThat(ids(content(managed(scenario), 5))).containsExactlyInAnyOrderElementsOf(mine.values());
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT",
        "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER", "CHAPTER_PRESIDENT"})
    void 다른_기수의_운영진_역할로는_관리_목록과_대리_등록에_접근할_수_없다(ChallengerRoleType role) throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherGisu = projects.다른_기수();
        Long actor = fixture.역할_회원(scenario, role, ChallengerPart.DESIGN);
        authenticate(actor);
        // when & then
        content(managed(otherGisu), 0);
        create(otherGisu, otherGisu.ownerId()).andExpect(status().isForbidden());
    }

    @Test
    void 최고관리자는_다른_기수도_조회하고_대리_등록할_수_있다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherGisu = projects.다른_기수();
        fixture.모든_상태(otherGisu);
        Long actor = projects.회원(scenario, "최고관리자", ChallengerPart.DESIGN);
        fixture.최고관리자(actor);
        authenticate(actor);
        // when & then
        content(managed(otherGisu), 4);
        create(otherGisu, otherGisu.ownerId()).andExpect(status().isOk());
    }

    @Test
    void 중앙운영진과_지부장을_겸하면_범위를_합친_뒤_정렬하고_페이지를_나눈다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario otherChapter = projects.다른_지부(scenario);
        var local = fixture.모든_상태(scenario);
        var foreign = fixture.모든_상태(otherChapter);
        Long actor = fixture.역할_회원(scenario, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER,
            ChallengerPart.DESIGN);
        fixture.역할_추가(scenario, actor, ChallengerRoleType.CHAPTER_PRESIDENT);
        authenticate(actor);
        // when & then
        Set<Long> found = new HashSet<>();
        for (int page = 0; page < 3; page++) {
            List<JsonNode> rows = content(managed(scenario).param("size", "2").param("page", String.valueOf(page)), 6);
            assertThat(rows).hasSize(2);
            found.addAll(ids(rows));
        }
        assertThat(found).hasSize(6).contains(local.get(ProjectStatus.COMPLETED), local.get(ProjectStatus.ABORTED))
            .doesNotContain(foreign.get(ProjectStatus.COMPLETED), foreign.get(ProjectStatus.ABORTED),
                local.get(ProjectStatus.DRAFT), foreign.get(ProjectStatus.DRAFT));
        assertThat(ids(content(managed(scenario).param("keyword", "COMPLETED"), 1)))
            .containsExactly(local.get(ProjectStatus.COMPLETED));
    }

    @Test
    void 중앙운영진의_목록_조회_허용이_승인_삭제_수동_매칭_권한을_늘리지_않는다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        var states = fixture.모든_상태(scenario);
        Long actor = fixture.역할_회원(scenario, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER,
            ChallengerPart.DESIGN);
        authenticate(actor);
        // when & then
        content(managed(scenario), 2);
        mockMvc.perform(post("/api/v1/projects/{id}/publish", states.get(ProjectStatus.PENDING_REVIEW)))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/projects/{id}", scenario.projectId())).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/projects/admin/{id}/members", scenario.projectId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("memberId", scenario.candidateId(), "part", "DESIGN"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void 총괄단은_선택한_지부만_필터하고_페이지_합계에도_반영한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario other = projects.다른_지부(scenario);
        fixture.모든_상태(scenario);
        fixture.모든_상태(other);
        Long actor = fixture.역할_회원(scenario, ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerPart.DESIGN);
        authenticate(actor);

        // when & then
        String body = mockMvc.perform(managed(scenario).param("chapterId", other.chapterId().toString()).param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(4))
            .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(body).path("result").path("content").get(0).path("id").asLong())
            .isEqualTo(other.projectId());
    }

    @Test
    void 지부장에게_다른_지부를_요청하면_본인_지부_목록으로_대체하지_않고_빈_목록을_준다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario other = projects.다른_지부(scenario);
        authenticate(scenario.staffId());

        // when & then
        content(managed(scenario).param("chapterId", other.chapterId().toString()), 0);
        assertThat(content(managed(scenario).param("chapterId", scenario.chapterId().toString()), 1)
            .get(0).path("id").asLong()).isEqualTo(scenario.projectId());
    }

    @Test
    void PM의_지부_필터는_본인_프로젝트_범위를_넓히지_않는다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario other = projects.다른_지부(scenario);
        authenticate(scenario.ownerId());

        // when & then
        content(managed(scenario).param("chapterId", other.chapterId().toString()), 0);
        content(managed(scenario).param("chapterId", scenario.chapterId().toString()), 1);
    }

    @Test
    void 지부장과_중앙운영진_겸직도_선택한_지부의_허용_상태만_조회한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario other = projects.다른_지부(scenario);
        fixture.모든_상태(scenario);
        fixture.모든_상태(other);
        fixture.역할_추가(scenario, scenario.staffId(), ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER);
        authenticate(scenario.staffId());

        // when & then
        List<JsonNode> rows = content(managed(scenario).param("chapterId", other.chapterId().toString()), 2);
        assertThat(rows).extracting(row -> row.path("status").asText()).containsExactly("IN_PROGRESS", "PENDING_REVIEW");
        assertThat(rows).extracting(row -> row.path("id").asLong()).contains(other.projectId()).doesNotContain(scenario.projectId());
    }

    @Test
    void 다른_기수의_지부나_존재하지_않는_지부_필터는_빈_목록을_반환한다() throws Exception {
        // given
        Scenario scenario = projects.준비();
        Scenario other = projects.다른_기수();
        Long actor = fixture.역할_회원(scenario, ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerPart.DESIGN);
        authenticate(actor);

        // when & then
        content(managed(scenario).param("chapterId", other.chapterId().toString()), 0);
        content(managed(scenario).param("chapterId", Long.toString(Long.MAX_VALUE)), 0);
    }

    private List<JsonNode> content(MockHttpServletRequestBuilder request, int total) throws Exception {
        String body = mockMvc.perform(request).andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(total))
            .andReturn().getResponse().getContentAsString();
        return java.util.stream.StreamSupport.stream(objectMapper.readTree(body).path("result").path("content")
            .spliterator(), false).toList();
    }

    private Set<Long> ids(List<JsonNode> rows) {
        return rows.stream().map(row -> row.path("id").asLong()).collect(java.util.stream.Collectors.toSet());
    }

    private MockHttpServletRequestBuilder managed(Scenario scenario) {
        return get("/api/v1/projects/me/managed").param("gisuId", scenario.gisuId().toString());
    }

    private ResultActions create(Scenario scenario, Long target) throws Exception {
        return mockMvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of("gisuId", scenario.gisuId(), "productOwnerMemberId", target))));
    }

    private long projectCount() {
        return jdbc.queryForObject("SELECT count(*) FROM project", Long.class);
    }

    private void authenticate(Long memberId) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        MemberPrincipal principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
