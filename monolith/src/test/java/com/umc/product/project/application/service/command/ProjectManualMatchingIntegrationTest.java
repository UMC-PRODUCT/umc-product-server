package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.ManageProjectMatchingUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectMatchingResultUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.in.query.GetProjectPermissionsUseCase;
import com.umc.product.project.application.port.in.query.GetRandomMatchedProjectMemberUseCase;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.ApplicationScenario;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;

@AutoConfigureMockMvc(addFilters = false)
class ProjectManualMatchingIntegrationTest extends IntegrationTestSupport {
    @Autowired ProjectManualMatchingFixture fixture;
    @Autowired ManageProjectMatchingUseCase matching;
    @Autowired AutoDecideProjectMatchingRoundUseCase finalizer;
    @Autowired LoadProjectStatisticsPort statistics;
    @Autowired LoadProjectMemberPort members;
    @Autowired LoadProjectApplicationPort applications;
    @Autowired GetProjectPermissionsUseCase permissions;
    @Autowired GetRandomMatchedProjectMemberUseCase matchedMembers;
    @Autowired PublishProjectMatchingResultUseCase publisher;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    @AfterEach
    void 인증_정리() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void 매칭_차수가_없으면_수동_배정을_거부한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        authenticate(scenario.staffId());

        // when & then
        assign(scenario, scenario.candidateId()).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PROJECT-0103"));
        assertThat(members.existsByGisuAndMember(scenario.gisuId(), scenario.candidateId())).isFalse();
    }

    @Test
    void 허용_기간에_운영진이_즉시_배정하고_해제한_뒤_다시_배정한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        authenticate(scenario.staffId());
        mockMvc.perform(get(candidates(scenario)).param("part", "DESIGN"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(1));

        // when
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());

        // then
        assertThat(members.listByProjectId(scenario.projectId())).extracting(m -> m.getMemberId()).contains(scenario.candidateId());
        ProjectMember member = members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId());
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(member.getAssignedMatchingRound()).isNull();
        assertThat(statistics.listPublicActiveMembersByChapterId(scenario.chapterId())).isEmpty();
        mockMvc.perform(get(candidates(scenario)).param("part", "DESIGN"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(0));
        mockMvc.perform(delete(admin(scenario) + "/{memberId}", scenario.candidateId())).andExpect(status().isOk());
        assertThat(members.existsByGisuAndMember(scenario.gisuId(), scenario.candidateId())).isFalse();
        assertThat(statistics.listPublicActiveMembersByChapterId(scenario.chapterId())).isEmpty();
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
    }

    @Test
    void PM의_일반_팀원_조작은_기존_API에서도_막고_보조_PM_관리는_유지한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        authenticate(scenario.ownerId());

        // when & then
        assign(scenario, scenario.candidateId()).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/projects/{id}/members",
            scenario.projectId()).contentType(MediaType.APPLICATION_JSON)
                .content(body(scenario.candidateId(), "DESIGN"))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/projects/{id}/members",
            scenario.projectId()).contentType(MediaType.APPLICATION_JSON)
                .content(body(scenario.candidateId(), "PLAN"))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/projects/{id}/members",
            scenario.projectId()).contentType(MediaType.APPLICATION_JSON)
                .content(body(scenario.coPmId(), "PLAN"))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/projects/{id}/members/{member}", scenario.projectId(), scenario.coPmId()))
            .andExpect(status().isOk());
        authenticate(scenario.staffId());
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
        authenticate(scenario.ownerId());
        mockMvc.perform(delete("/api/v1/projects/{id}/members/{member}", scenario.projectId(), scenario.candidateId()))
            .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(),
            scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISMISSED\",\"reason\":\"해제\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void 지부장은_다른_지부를_관리할_수_없고_총괄단은_관리할_수_있다() throws Exception {
        // given
        Scenario first = fixture.준비();
        Scenario other = fixture.다른_지부(first);
        fixture.배정_가능_기간(other);
        authenticate(first.staffId());

        // when & then
        assign(other, other.candidateId()).andExpect(status().isForbidden());
        mockMvc.perform(get(candidates(other)).param("part", "DESIGN")).andExpect(status().isForbidden());
        fixture.총괄단(first);
        authenticate(first.staffId());
        assign(other, other.candidateId()).andExpect(status().isOk());
    }

    @Test
    void 정원_초과와_다른_기수의_권한과_잘못된_파트와_다른_지부_후보를_거부한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        Scenario other = fixture.다른_지부(scenario);
        Long second = fixture.회원(scenario, "두번째", ChallengerPart.DESIGN);
        authenticate(scenario.staffId());

        // when & then
        mockMvc.perform(post(admin(scenario)).contentType(MediaType.APPLICATION_JSON)
                .content(body(scenario.candidateId(), "WEB_PRODUCT_ENGINEER"))).andExpect(status().isForbidden());
        assign(scenario, other.candidateId()).andExpect(status().isForbidden());
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
        assign(scenario, second).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PROJECT-0106"));
        // 같은 지부 ID여도 기수가 다른 역할로는 수동 매칭 권한을 얻지 못한다.
        var nextGisu = fixture.다른_기수();
        jdbc.update("UPDATE challenger_role SET gisu_id = ? WHERE organization_id = ?", nextGisu.gisuId(),
            scenario.chapterId());
        authenticate(scenario.staffId());
        mockMvc.perform(get(candidates(scenario)).param("part", "DESIGN")).andExpect(status().isForbidden());
        assertThat(members.listByProjectId(scenario.projectId())).hasSize(1);
    }

    @Test
    void 미반영_PM_합격자로_TO가_가득_차면_수동_배정을_거부한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        ApplicationScenario application = fixture.PM_합격_처리(scenario);
        Long candidateId = fixture.회원(scenario, "수동배정후보", ChallengerPart.DESIGN);
        authenticate(scenario.staffId());

        // when & then
        assign(scenario, candidateId).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PROJECT-0107"));
        assertThat(members.listByProjectId(scenario.projectId())).isEmpty();
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.APPROVED);
    }

    @Test
    void 미반영_PM_합격자를_제외한_남은_TO에만_수동_배정한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        jdbc.update("UPDATE project_part_quota SET quota = 2 WHERE project_id = ?", scenario.projectId());
        fixture.PM_합격_처리(scenario);
        Long first = fixture.회원(scenario, "첫번째후보", ChallengerPart.DESIGN);
        Long second = fixture.회원(scenario, "두번째후보", ChallengerPart.DESIGN);
        authenticate(scenario.staffId());

        // when & then
        assign(scenario, first).andExpect(status().isOk());
        assign(scenario, second).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PROJECT-0107"));
        assertThat(members.listByProjectId(scenario.projectId())).extracting(ProjectMember::getMemberId)
            .containsExactly(first);
    }

    @Test
    void 공개된_팀원의_합격_지원서는_수동_배정_TO에서_중복_차감하지_않는다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        jdbc.update("UPDATE project_part_quota SET quota = 2 WHERE project_id = ?", scenario.projectId());
        ApplicationScenario application = fixture.지원서_배정(scenario, true);
        assertThat(publisher.publish(application.roundId())).isTrue();
        Long candidateId = fixture.회원(scenario, "수동배정후보", ChallengerPart.DESIGN);
        authenticate(scenario.staffId());

        // when & then
        assign(scenario, candidateId).andExpect(status().isOk());
        assertThat(members.listByProjectId(scenario.projectId())).extracting(ProjectMember::getMemberId)
            .containsExactlyInAnyOrder(scenario.candidateId(), candidateId);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 공개된_팀원을_해제하면_지원서를_불합격으로_바꾸고_답변을_보존한다(boolean pending) throws Exception {
        // given
        Scenario scenario = fixture.준비();
        ApplicationScenario application = fixture.지원서_배정(scenario, pending);
        if (pending) {
            assertThat(publisher.publish(application.roundId())).isTrue();
        }
        authenticate(scenario.staffId());

        // when
        mockMvc.perform(delete(admin(scenario) + "/{memberId}", scenario.candidateId())).andExpect(status().isOk());

        // then
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM answer WHERE id = ?", Long.class, application.answerId()))
            .isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM form_response WHERE id = ?", Long.class,
            application.responseId()))
            .isEqualTo(1L);
        assertThat(members.existsByGisuAndMember(scenario.gisuId(), scenario.candidateId())).isFalse();
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
    }

    @Test
    void 일반_차수의_공개_전에는_해제를_거부하고_팀원과_지원서_상태를_유지한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        ApplicationScenario application = fixture.지원서_배정(scenario, true);
        authenticate(scenario.staffId());

        // when
        mockMvc.perform(delete(admin(scenario) + "/{memberId}", scenario.candidateId()))
            .andExpect(status().isConflict());

        // then
        assertThat(members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId()).getStatus())
            .isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.APPROVED);
    }

    @Test
    void 상태변경으로_퇴출한_팀원은_결과_공개_후_배정_API로_재배정한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        ApplicationScenario application = fixture.지원서_배정(scenario, true);
        authenticate(scenario.staffId());

        // when
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(),
            scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISMISSED\",\"reason\":\"해제\"}"))
            .andExpect(status().isOk());

        // then
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.REJECTED);
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(),
            scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\",\"reason\":\"재배정\"}"))
            .andExpect(status().isConflict());
        assertThat(publisher.publish(application.roundId())).isTrue();
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
        ProjectMember member = members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId());
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(member.getAssignedMatchingRound()).isNull();
        assertThat(member.getApplication()).isNull();
        assertThat(members.listByProjectId(scenario.projectId())).hasSize(1);
    }

    @Test
    void 동일한_퇴출_요청은_팀원과_연결_지원서의_사유와_처리_시각을_유지한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        ApplicationScenario application = fixture.지원서_배정(scenario, true);
        authenticate(scenario.staffId());
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(), scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISMISSED\",\"reason\":\"퇴출 사유\"}"))
            .andExpect(status().isOk());
        Instant memberUpdatedAt = members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId()).getStatusUpdatedAt();
        Instant applicationChangedAt = applications.findById(application.applicationId()).orElseThrow().getStatusChangedAt();

        // when
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(), scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISMISSED\",\"reason\":\"변경된 사유\"}"))
            .andExpect(status().isOk());

        // then
        ProjectMember member = members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId());
        ProjectApplication rejected = applications.findById(application.applicationId()).orElseThrow();
        assertThat(member.getStatusChangeReason()).isEqualTo("퇴출 사유");
        assertThat(member.getStatusUpdatedAt()).isEqualTo(memberUpdatedAt);
        assertThat(rejected.getStatusChangeReason()).isEqualTo("퇴출 사유");
        assertThat(rejected.getStatusChangedAt()).isEqualTo(applicationChangedAt);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WEB", "WEB_PRODUCT_ENGINEER", "MOBILE_PRODUCT_ENGINEER"})
    void 개발_파트의_수동_배정_결과를_본인이_즉시_조회할_수_있다(String partName) throws Exception {
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario, MatchingType.PLAN_DEVELOPER);
        ChallengerPart part = ChallengerPart.valueOf(partName);
        Long memberId = fixture.회원(scenario, "개발자", part);
        jdbc.update("UPDATE project_part_quota SET part = ? WHERE project_id = ?", partName, scenario.projectId());
        authenticate(scenario.staffId());
        mockMvc.perform(post(admin(scenario)).contentType(MediaType.APPLICATION_JSON).content(body(memberId, partName)))
            .andExpect(status().isOk());
        assertThat(matchedMembers.findRandomMatched(memberId, scenario.gisuId())).isPresent();
        mockMvc.perform(delete(admin(scenario) + "/{memberId}", memberId)).andExpect(status().isOk());
        assertThat(matchedMembers.findRandomMatched(memberId, scenario.gisuId())).isEmpty();
    }

    @Test
    void 팀원의_정상_완료는_기존_합격_이력을_보존한다() throws Exception {
        Scenario scenario = fixture.준비();
        var application = fixture.지원서_배정(scenario, false);
        authenticate(scenario.staffId());
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status",
                scenario.projectId(), scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"reason\":\"정상 완료\"}"))
            .andExpect(status().isOk());
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId()).getStatus())
            .isEqualTo(ProjectMemberStatus.COMPLETED);
    }

    @Test
    void 발표_전_인원도_정원에_포함하고_합격_지원서_보유자는_후보에서_제외한다() throws Exception {
        // given
        Scenario scenario = fixture.준비();
        fixture.지원서_배정(scenario, true);
        Long second = fixture.회원(scenario, "추가후보", ChallengerPart.DESIGN);
        authenticate(scenario.staffId());

        // when & then
        assign(scenario, second).andExpect(status().isConflict());
        jdbc.update("DELETE FROM project_member WHERE member_id = ?", scenario.candidateId());
        mockMvc.perform(get(candidates(scenario)).param("part", "DESIGN"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].memberId").value(second.toString()));
        assign(scenario, scenario.candidateId()).andExpect(status().isConflict());
    }

    @Test
    void 매칭지부를_옮긴_후보는_이동한_지부에서만_조회하고_배정한다() throws Exception {
        Scenario from = fixture.준비();
        fixture.배정_가능_기간(from);
        Scenario to = fixture.다른_지부(from);
        fixture.배정_가능_기간(to);
        fixture.매칭지부_이동(from, to);
        authenticate(from.staffId());
        mockMvc.perform(get(candidates(from)).param("part", "DESIGN"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(0));
        assign(from, from.candidateId()).andExpect(status().isForbidden());
        authenticate(to.staffId());
        mockMvc.perform(get(candidates(to)).param("part", "DESIGN"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result.totalElements").value(2));
        assign(to, from.candidateId()).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMPLETED", "ABORTED"})
    void 종료된_프로젝트는_운영진도_배정이나_해제할_수_없다(String state) throws Exception {
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        authenticate(scenario.staffId());
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
        jdbc.update("UPDATE project SET status = ? WHERE id = ?", state, scenario.projectId());
        assign(scenario, scenario.candidateId()).andExpect(status().isBadRequest());
        mockMvc.perform(delete(admin(scenario) + "/{memberId}",
            scenario.candidateId())).andExpect(status().isBadRequest());
    }

    @Test
    void 수동_배정된_지원자는_다른_프로젝트에_자동_선발되지_않는다() throws Exception {
        Scenario scenario = fixture.준비();
        var app = fixture.지원서_배정(scenario, false);
        jdbc.update("DELETE FROM project_member WHERE member_id = ?", scenario.candidateId());
        jdbc.update("UPDATE project_application SET status = 'SUBMITTED' WHERE id = ?", app.applicationId());
        jdbc.update("UPDATE project_matching_round SET auto_decision_executed_at = NULL WHERE id = ?", app.roundId());
        Long otherProjectId = fixture.프로젝트(scenario, 1L);
        matching.assign(AddProjectMemberCommand.builder().projectId(otherProjectId).memberId(scenario.candidateId())
            .part(ChallengerPart.DESIGN).requesterMemberId(scenario.staffId()).build());
        finalizer.autoDecide(app.roundId(), null);
        assertThat(members.listAssignedMemberIdsByGisuId(scenario.gisuId())).containsExactly(scenario.candidateId());
        assertThat(members.listByProjectId(scenario.projectId())).isEmpty();
        assertThat(applications.findById(app.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.REJECTED);
    }

    @Test
    void 재배정_시_변경된_현재_파트를_팀원과_정원_검증에_적용한다() throws Exception {
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        authenticate(scenario.staffId());
        assign(scenario, scenario.candidateId()).andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/projects/{id}/members/{member}/status", scenario.projectId(),
            scenario.candidateId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISMISSED\",\"reason\":\"해제\"}"))
            .andExpect(status().isOk());
        jdbc.update("UPDATE challenger SET part = 'WEB_PRODUCT_ENGINEER' WHERE member_id = ?", scenario.candidateId());
        jdbc.update("UPDATE project_part_quota SET part = 'WEB_PRODUCT_ENGINEER' WHERE project_id = ?",
            scenario.projectId());
        jdbc.update("UPDATE project_matching_round SET type = 'PLAN_DEVELOPER' WHERE chapter_id = ?", scenario.chapterId());
        mockMvc.perform(post(admin(scenario)).contentType(MediaType.APPLICATION_JSON)
                .content(body(scenario.candidateId(), "WEB_PRODUCT_ENGINEER"))).andExpect(status().isOk());
        assertThat(members.getByProjectIdAndMemberId(scenario.projectId(), scenario.candidateId()).getPart())
            .isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
    }

    @Test
    void 권한_응답은_보조_PM_관리와_수동_매칭을_구분한다() {
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        var pm = permissions.listByProjectIds(scenario.ownerId(), java.util.List.of(scenario.projectId())).getFirst();
        var staff = permissions.listByProjectIds(scenario.staffId(),
            java.util.List.of(scenario.projectId())).getFirst();
        assertThat(pm.member().canCreate().allowed()).isTrue();
        assertThat(pm.member().canAssign().allowed()).isFalse();
        assertThat(pm.member().canUnassign().allowed()).isFalse();
        assertThat(staff.member().canAssign().allowed()).isTrue();
        assertThat(staff.member().canUnassign().allowed()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 동시_요청도_중복_배정이나_정원_초과를_만들지_않는다(boolean sameCandidate) throws Exception {
        // given
        Scenario scenario = fixture.준비();
        fixture.배정_가능_기간(scenario);
        Long otherProject = sameCandidate ? fixture.프로젝트(scenario, 1L) : scenario.projectId();
        Long otherCandidate = sameCandidate ? scenario.candidateId() : fixture.회원(scenario, "동시후보",
            ChallengerPart.DESIGN);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var race = new PostgreSqlTransactionRace(transactionManager, jdbc)) {
            Supplier<Boolean> first = () -> attempt(race, ready, start, scenario, scenario.projectId(),
                scenario.candidateId());
            Supplier<Boolean> second = () -> attempt(race, ready, start, scenario, otherProject, otherCandidate);
            var one = race.submit(first);
            var two = race.submit(second);
            race.await(ready, "두 배정 트랜잭션 준비");
            start.countDown();
            boolean firstSuccess = succeeded(one.future());
            boolean secondSuccess = succeeded(two.future());
            assertThat(firstSuccess ^ secondSuccess).isTrue();
        }
        assertThat(members.listAssignedMemberIdsByGisuId(scenario.gisuId())).hasSize(1);
    }

    @Test
    void 해제_도중_실패하면_지원서와_팀원_변경을_모두_롤백한다() {
        Scenario scenario = fixture.준비();
        var application = fixture.지원서_배정(scenario, false);
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).execute(status -> {
            matching.unassign(RemoveProjectMemberCommand.builder().projectId(scenario.projectId())
                .memberId(scenario.candidateId()).requesterMemberId(scenario.staffId()).build());
            throw new IllegalStateException("후속 작업 실패");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(applications.findById(application.applicationId()).orElseThrow().getStatus())
            .isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(members.existsByGisuAndMember(scenario.gisuId(), scenario.candidateId())).isTrue();
    }

    private boolean attempt(PostgreSqlTransactionRace race, CountDownLatch ready, CountDownLatch start,
        Scenario scenario, Long projectId, Long memberId) {
        ready.countDown();
        race.await(start, "동시 배정 시작");
        matching.assign(AddProjectMemberCommand.builder().projectId(projectId).memberId(memberId)
            .part(ChallengerPart.DESIGN).requesterMemberId(scenario.staffId()).build());
        return true;
    }

    private boolean succeeded(Future<Boolean> future) throws Exception {
        try {
            return future.get(20, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode").isIn(
                    ProjectErrorCode.PROJECT_MANUAL_MATCHING_QUOTA_EXCEEDED,
                    ProjectErrorCode.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM);
            return false;
        }
    }

    private ResultActions assign(Scenario scenario, Long memberId) throws Exception {
        return mockMvc.perform(post(admin(scenario)).contentType(MediaType.APPLICATION_JSON).content(body(memberId, "DESIGN")));
    }

    private String body(Long memberId, String part) throws Exception {
        return objectMapper.writeValueAsString(Map.of("memberId", memberId, "part", part));
    }

    private String admin(Scenario scenario) {
        return "/api/v1/projects/admin/" + scenario.projectId() + "/members";
    }

    private String candidates(Scenario scenario) {
        return admin(scenario) + "/candidates";
    }

    private void authenticate(Long memberId) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        var principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
