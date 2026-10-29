package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ProjectDraftFixture;
import com.umc.product.support.fixture.ProjectManagementPermissionFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.ProjectQuotaManagementFixture;

@AutoConfigureMockMvc(addFilters = false)
class ProjectSubmissionIntegrationTest extends IntegrationTestSupport {
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectManagementPermissionFixture permissions;
    @Autowired ProjectDraftFixture drafts;
    @Autowired ProjectQuotaManagementFixture quotas;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;
    private Scenario scenario;

    @BeforeEach
    void 등록할_지부와_인원을_준비한다() {
        scenario = projects.준비();
        quotas.정원(scenario.projectId(), ChallengerPart.DESIGN, 0);
    }

    @AfterEach
    void 인증과_권한_캐시를_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class,
        names = {"CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT", "CHAPTER_PRESIDENT"})
    void 운영진_직접_등록은_지원폼과_함께_공개하고_목록에_반영한다(ChallengerRoleType role) throws Exception {
        // given
        Long actor = permissions.역할_회원(scenario, role, ChallengerPart.DESIGN);
        Long id = prepare(actor, scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/projects/permissions").param("ids", id.toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.projects[0].partQuota.canEdit.allowed").value(true));

        // when
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("IN_PROGRESS"));

        // then
        assertThat(projectStatus(id)).isEqualTo("IN_PROGRESS");
        assertThat(formStatus(id)).isEqualTo("PUBLISHED");
        assertThat(jdbc.queryForObject("SELECT submitted_at IS NOT NULL FROM project WHERE id=?", Boolean.class, id)).isTrue();
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/projects").param("gisuId", scenario.gisuId().toString())
                .param("keyword", "직접 등록 테스트"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.content[0].id").value(id.toString()));
    }

    @Test
    void 다른_기수의_최고관리자도_직접_등록할_수_있다() throws Exception {
        // given
        Scenario other = projects.다른_기수();
        permissions.최고관리자(other.ownerId());
        Long id = prepare(other.ownerId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());

        // when & then
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("IN_PROGRESS"));
        assertThat(formStatus(id)).isEqualTo("PUBLISHED");
    }

    @Test
    void 일반_PM_제출은_TO_없이도_승인대기를_유지한다() throws Exception {
        // given
        Long id = prepare(scenario.ownerId(), scenario.ownerId());

        // when & then
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("PENDING_REVIEW"));
        assertThat(projectStatus(id)).isEqualTo("PENDING_REVIEW");
        assertThat(formStatus(id)).isEqualTo("DRAFT");
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"CENTRAL_OPERATING_TEAM_MEMBER",
        "CENTRAL_EDUCATION_TEAM_MEMBER", "SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT", "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN"})
    void 일반_중앙운영진과_학교_운영진이_PM이면_자동_승인하지_않는다(ChallengerRoleType role) throws Exception {
        // given
        Long actor = permissions.역할_회원(scenario, role, ChallengerPart.PLAN);
        Long id = prepare(actor, actor);

        // when & then
        quota(id, 1).andExpect(status().isForbidden());
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("PENDING_REVIEW"));
        assertThat(formStatus(id)).isEqualTo("DRAFT");
    }

    @Test
    void 운영진이_본인을_PM으로_등록해도_관리_권한이_있으면_즉시_공개한다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.staffId());
        quota(id, 1).andExpect(status().isOk());

        // when & then
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("IN_PROGRESS"));
    }

    @Test
    void 제출_전에_운영진_권한이_사라지면_대리_등록을_완료할_수_없다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        jdbc.update("DELETE FROM challenger_role WHERE challenger_id IN (SELECT id FROM challenger WHERE member_id=?)", scenario.staffId());
        authenticate(scenario.staffId());

        // when & then
        mockMvc.perform(get("/api/v1/projects/permissions").param("ids", id.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects[0].status.canRequestReview.allowed").value(false));
        submit(id).andExpect(status().isForbidden());
        assertDraft(id);
    }

    @Test
    void 다른_지부의_역할만_남으면_대리_등록을_완료할_수_없다() throws Exception {
        // given
        Scenario other = projects.다른_지부(scenario);
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        jdbc.update("UPDATE challenger_role SET organization_id=? WHERE challenger_id IN (SELECT id FROM challenger WHERE member_id=?)",
            other.chapterId(), scenario.staffId());
        authenticate(scenario.staffId());

        // when & then
        quota(id, 1).andExpect(status().isForbidden());
        submit(id).andExpect(status().isForbidden());
        assertDraft(id);
    }

    @Test
    void 다른_기수의_운영진_역할은_본인_PM_제출을_자동_승인하지_않는다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.staffId());
        Scenario other = projects.다른_기수();
        jdbc.update("UPDATE challenger_role SET gisu_id=? WHERE challenger_id IN (SELECT id FROM challenger WHERE member_id=?)",
            other.gisuId(), scenario.staffId());
        authenticate(scenario.staffId());

        // when & then
        submit(id).andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("PENDING_REVIEW"));
        assertThat(formStatus(id)).isEqualTo("DRAFT");
    }

    @Test
    void TO가_없으면_자동_공개를_거부하고_초안으로_롤백한다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());

        // when & then
        mockMvc.perform(get("/api/v1/projects/permissions").param("ids", id.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects[0].status.canRequestReview.allowed").value(false))
            .andExpect(jsonPath("$.result.projects[0].status.canRequestReview.reasonCode").value("PART_QUOTA_REQUIRED"));
        submit(id).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PROJECT-0202"));
        assertDraft(id);
    }

    @Test
    void 공개되는_프로젝트까지_합쳐_지부_TO_상한을_초과하면_전체_롤백한다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 2).andExpect(status().isOk());

        // when & then
        submit(id).andExpect(status().isConflict());
        assertDraft(id);
    }

    @Test
    void 지원폼_공개가_실패해도_프로젝트_상태와_제출시각을_롤백한다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        jdbc.update("UPDATE form SET status='PUBLISHED' WHERE id=(SELECT form_id FROM project_application_form WHERE project_id=?)", id);

        // when & then
        submit(id).andExpect(status().isBadRequest());
        assertThat(projectStatus(id)).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT submitted_at IS NULL FROM project WHERE id=?", Boolean.class, id)).isTrue();
    }

    @Test
    void 다른_PM이나_운영진은_작성자가_아닌_초안을_제출하거나_TO를_설정할_수_없다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        authenticate(scenario.ownerId());
        submit(id).andExpect(status().isForbidden());
        authenticate(permissions.역할_회원(scenario, ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerPart.DESIGN));

        // when & then
        quota(id, 1).andExpect(status().isForbidden());
        submit(id).andExpect(status().isForbidden());
        assertDraft(id);
    }

    @Test
    void 이미_등록한_프로젝트를_다시_제출할_수_없다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        submit(id).andExpect(status().isOk());

        // when & then
        submit(id).andExpect(status().isBadRequest());
        assertThat(projectStatus(id)).isEqualTo("IN_PROGRESS");
        assertThat(formStatus(id)).isEqualTo("PUBLISHED");
    }

    @Test
    void 지원폼이_없으면_PM_제출을_거부한다() throws Exception {
        // given
        Long id = prepare(scenario.ownerId(), scenario.ownerId());
        jdbc.update("DELETE FROM project_application_form WHERE project_id=?", id);

        // when & then
        submit(id).andExpect(status().isBadRequest());
        assertThat(projectStatus(id)).isEqualTo("DRAFT");
    }

    @Test
    void 프로젝트명이_없으면_운영진도_등록할_수_없다() throws Exception {
        // given
        Long id = prepare(scenario.staffId(), scenario.ownerId());
        quota(id, 1).andExpect(status().isOk());
        jdbc.update("UPDATE project SET name=NULL WHERE id=?", id);

        // when & then
        submit(id).andExpect(status().isBadRequest());
        assertDraft(id);
    }

    private Long prepare(Long actor, Long owner) throws Exception {
        authenticate(actor);
        String body = mockMvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("gisuId", scenario.gisuId(), "productOwnerMemberId", owner))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.status").value("DRAFT"))
            .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).path("result").path("projectId").asLong();
        mockMvc.perform(patch("/api/v1/projects/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"직접 등록 테스트\"}"))
            .andExpect(status().isOk());
        drafts.지원폼(id, actor);
        assertThat(formStatus(id)).isEqualTo("DRAFT");
        return id;
    }

    private ResultActions quota(Long id, long count) throws Exception {
        return mockMvc.perform(put("/api/v1/projects/{id}/part-quotas", id).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of("entries", List.of(Map.of("part", "DESIGN", "quota", count))))));
    }

    private ResultActions submit(Long id) throws Exception {
        return mockMvc.perform(post("/api/v1/projects/{id}/submit", id));
    }

    private void assertDraft(Long id) {
        assertThat(projectStatus(id)).isEqualTo("DRAFT");
        assertThat(formStatus(id)).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT submitted_at IS NULL FROM project WHERE id=?", Boolean.class, id)).isTrue();
    }

    private String projectStatus(Long id) {
        return jdbc.queryForObject("SELECT status FROM project WHERE id=?", String.class, id);
    }

    private String formStatus(Long id) {
        return jdbc.queryForObject("SELECT status FROM form WHERE id=(SELECT form_id FROM project_application_form WHERE project_id=?)", String.class, id);
    }

    private void authenticate(Long actor) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(actor);
        var principal = MemberPrincipal.builder().memberId(actor).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
