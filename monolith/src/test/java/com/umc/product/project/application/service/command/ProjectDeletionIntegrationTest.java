package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.form.domain.Answer;
import com.umc.product.form.domain.Form;
import com.umc.product.form.domain.FormResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectMatchingResultUseCase;
import com.umc.product.project.application.port.in.command.dto.DeleteProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.fixture.ProjectDeletionFixture;
import com.umc.product.support.fixture.ProjectDeletionFixture.Scenario;

@AutoConfigureMockMvc(addFilters = false)
class ProjectDeletionIntegrationTest extends IntegrationTestSupport {

    @Autowired ProjectDeletionFixture fixture;
    @Autowired DeleteProjectUseCase deleteProjectUseCase;
    @Autowired AutoDecideProjectMatchingRoundUseCase autoDecideProjectMatchingRoundUseCase;
    @Autowired LoadProjectStatisticsPort loadProjectStatisticsPort;
    @Autowired PublishProjectMatchingResultUseCase publishProjectMatchingResultUseCase;
    @Autowired LoadProjectPort loadProjectPort;
    @Autowired LoadProjectMemberPort loadProjectMemberPort;
    @Autowired LoadProjectApplicationPort loadProjectApplicationPort;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;

    @AfterEach
    void 인증_정리() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 등록완료_프로젝트와_지원서_답변_배정을_함께_삭제한다(boolean pendingPublication) throws Exception {
        // given
        Long ownerId = fixture.회원("소유자");
        Long applicantId = fixture.회원("지원자");
        Scenario scenario = fixture.삭제대상(ownerId, applicantId, pendingPublication, false);
        authenticate(ownerId);
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId())).contains(applicantId);

        // when
        mockMvc.perform(delete("/api/v1/projects/{projectId}", scenario.projectId())).andExpect(status().isOk());

        // then — 합격 근거와 배정이 모두 제거되어 다시 미배정 후보가 된다.
        entityManager.clear();
        assertDeleted(scenario);
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId())).doesNotContain(applicantId);
        assertThat(loadProjectApplicationPort.listByMatchingRoundId(scenario.roundId())).isEmpty();
        assertThat(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(scenario.projectId()))).isEmpty();
        assertThat(loadProjectStatisticsPort.listPublicActiveMembersByChapterId(scenario.chapterId())).isEmpty();
        autoDecideProjectMatchingRoundUseCase.autoDecide(scenario.roundId(), null);
        assertThat(publishProjectMatchingResultUseCase.publish(scenario.roundId())).isEqualTo(pendingPublication);
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId())).doesNotContain(applicantId);
    }

    @Test
    void 다른_프로젝트의_배정은_유지한다() {
        // given
        Long ownerId = fixture.회원("소유자");
        Long applicantId = fixture.회원("지원자");
        Scenario scenario = fixture.삭제대상(ownerId, applicantId, false, true);
        authenticate(ownerId);

        // when
        deleteProjectUseCase.delete(command(scenario, ownerId));

        // then
        assertThat(loadProjectPort.findById(scenario.otherProjectId())).isPresent();
        assertThat(loadProjectMemberPort.listByProjectId(scenario.otherProjectId()))
            .singleElement().extracting(member -> member.getMemberId()).isEqualTo(applicantId);
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId())).contains(applicantId);
    }

    @Test
    void 지원기간의_직접_API_삭제도_차단한다() throws Exception {
        // given
        Long ownerId = fixture.회원("소유자");
        Scenario scenario = fixture.삭제대상(ownerId, fixture.회원("지원자"), false, false);
        fixture.지원기간(scenario.chapterId());
        authenticate(ownerId);

        // when & then
        mockMvc.perform(delete("/api/v1/projects/{projectId}", scenario.projectId()))
            .andExpect(status().isConflict());
        assertThat(loadProjectPort.findById(scenario.projectId())).isPresent();
        assertThat(entityManager.find(Answer.class, scenario.answerId())).isNotNull();
    }

    @Test
    void 삭제_권한이_없는_사용자는_API에서_거부한다() throws Exception {
        // given
        Long ownerId = fixture.회원("소유자");
        Scenario scenario = fixture.삭제대상(ownerId, fixture.회원("지원자"), false, false);
        authenticate(fixture.회원("다른사용자"));

        // when & then
        mockMvc.perform(delete("/api/v1/projects/{projectId}", scenario.projectId())).andExpect(status().isForbidden());
        assertThat(loadProjectPort.findById(scenario.projectId())).isPresent();
    }

    @Test
    void 트랜잭션_실패시_지원서_답변_배정을_모두_복구한다() {
        // given
        Long ownerId = fixture.회원("소유자");
        Scenario scenario = fixture.삭제대상(ownerId, fixture.회원("지원자"), false, false);
        authenticate(ownerId);

        // when
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(ignored -> {
            deleteProjectUseCase.delete(command(scenario, ownerId));
            throw new IllegalStateException("후속 처리 실패");
        })).isInstanceOf(IllegalStateException.class).hasMessage("후속 처리 실패");

        // then
        entityManager.clear();
        assertThat(loadProjectPort.findById(scenario.projectId())).isPresent();
        assertThat(entityManager.find(ProjectApplication.class, scenario.applicationId())).isNotNull();
        assertThat(entityManager.find(FormResponse.class, scenario.responseId())).isNotNull();
        assertThat(entityManager.find(Answer.class, scenario.answerId())).isNotNull();
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId())).contains(scenario.applicantId());
    }

    @Test
    void 결과_발표가_진행중이면_기다린_뒤_삭제해_배정이_남지_않는다() throws Exception {
        // given
        Long ownerId = fixture.회원("소유자");
        Scenario scenario = fixture.삭제대상(ownerId, fixture.회원("지원자"), true, false);
        fixture.자동선발완료(scenario.roundId());
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        // when
        try (var race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            var publication = race.submit(() -> {
                loadProjectMatchingRoundPort.getByIdForUpdate(scenario.roundId());
                locked.countDown();
                race.await(release, "발표 트랜잭션 해제");
                return publishProjectMatchingResultUseCase.publish(scenario.roundId());
            });
            race.await(locked, "발표 차수 잠금");
            var deletion = race.submit(() -> {
                authenticate(ownerId);
                try {
                    deleteProjectUseCase.delete(command(scenario, ownerId));
                    return true;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });
            try {
                race.awaitPostgreSqlLockWait(deletion);
            } finally {
                release.countDown();
            }
            assertThat(publication.future().get(10, TimeUnit.SECONDS)).isTrue();
            assertThat(deletion.future().get(10, TimeUnit.SECONDS)).isTrue();
        }

        // then
        entityManager.clear();
        assertDeleted(scenario);
        assertThat(loadProjectMemberPort.listAssignedMemberIdsByGisuId(scenario.gisuId()))
            .doesNotContain(scenario.applicantId());
    }

    private void assertDeleted(Scenario scenario) {
        assertThat(loadProjectPort.findById(scenario.projectId())).isEmpty();
        assertThat(loadProjectMemberPort.listByProjectId(scenario.projectId())).isEmpty();
        assertThat(entityManager.find(ProjectApplication.class, scenario.applicationId())).isNull();
        assertThat(entityManager.find(Form.class, scenario.formId())).isNull();
        assertThat(entityManager.find(FormResponse.class, scenario.responseId())).isNull();
        assertThat(entityManager.find(Answer.class, scenario.answerId())).isNull();
    }

    private DeleteProjectCommand command(Scenario scenario, Long ownerId) {
        return DeleteProjectCommand.builder().projectId(scenario.projectId()).requesterMemberId(ownerId).build();
    }

    private void authenticate(Long memberId) {
        MemberPrincipal principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
