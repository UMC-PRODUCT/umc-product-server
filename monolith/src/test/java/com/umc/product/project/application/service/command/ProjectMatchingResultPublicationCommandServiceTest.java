package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingResultPublicationCommandServiceTest {

    private static final Long ROUND_ID = 1L;
    private static final Long EXECUTOR_ID = 999L;

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    SaveProjectApplicationPort saveProjectApplicationPort;
    @InjectMocks
    ProjectMatchingResultPublicationCommandService sut;

    @Test
    void 선발된_지원서는_합격으로_나머지_지원서는_불합격으로_발표한다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        Project project = project();
        ProjectApplication selected = application(project, round, 10L, ProjectApplicationStatus.REJECTED);
        ProjectApplication submitted = application(project, round, 11L, ProjectApplicationStatus.SUBMITTED);
        ProjectApplication approved = application(project, round, 12L, ProjectApplicationStatus.APPROVED);
        ProjectMember member = ProjectMember.createFromApplication(selected, ChallengerPart.WEB_PRODUCT_ENGINEER, EXECUTOR_ID);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);
        given(loadProjectMemberPort.listPendingByMatchingRoundId(ROUND_ID)).willReturn(List.of(member));
        given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of(selected, submitted, approved));

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isTrue();
        assertThat(selected.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(submitted.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(approved.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(selected.getStatusChangedMemberId()).isEqualTo(EXECUTOR_ID);
        assertThat(selected.getStatusChangeReason()).isEqualTo("auto-decide");
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(round.getResultPublishedAt()).isNotNull();
        then(saveProjectApplicationPort).should().saveAll(List.of(selected, submitted, approved));
    }

    @Test
    void 선발된_멤버가_없어도_지원서를_불합격으로_발표한다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        ProjectApplication application = application(project(), round, 10L, ProjectApplicationStatus.SUBMITTED);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);
        given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of(application));

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isTrue();
        assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        assertThat(round.getResultPublishedAt()).isNotNull();
        then(saveProjectApplicationPort).should().saveAll(List.of(application));
    }

    @Test
    void 지원서_없는_랜덤_배정_멤버도_활성화한다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        ProjectMember member = ProjectMember.createForMatching(
            project(), 10L, ChallengerPart.WEB_PRODUCT_ENGINEER, round, EXECUTOR_ID);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);
        given(loadProjectMemberPort.listPendingByMatchingRoundId(ROUND_ID)).willReturn(List.of(member));

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isTrue();
        assertThat(member.getApplication()).isNull();
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(round.getResultPublishedAt()).isNotNull();
    }

    @Test
    void 임시저장과_취소된_지원서는_발표_대상에서_제외한다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        Project project = project();
        ProjectApplication draft = application(project, round, 10L, ProjectApplicationStatus.DRAFT);
        ProjectApplication cancelled = application(project, round, 11L, ProjectApplicationStatus.CANCELLED);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);
        given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of(draft, cancelled));

        // when
        sut.publish(ROUND_ID);

        // then
        assertThat(draft.getStatus()).isEqualTo(ProjectApplicationStatus.DRAFT);
        assertThat(cancelled.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        then(saveProjectApplicationPort).should().saveAll(List.of());
    }

    @Test
    void 중단된_프로젝트의_멤버와_지원서는_변경하지_않는다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        Project project = project();
        ProjectApplication application = application(project, round, 10L, ProjectApplicationStatus.APPROVED);
        ProjectMember member = ProjectMember.createFromApplication(application, ChallengerPart.WEB_PRODUCT_ENGINEER, EXECUTOR_ID);
        project.abort("프로젝트 중단", EXECUTOR_ID);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);
        given(loadProjectMemberPort.listPendingByMatchingRoundId(ROUND_ID)).willReturn(List.of(member));
        given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of(application));

        // when
        sut.publish(ROUND_ID);

        // then
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        assertThat(application.getStatusChangedAt()).isNull();
        then(saveProjectApplicationPort).should().saveAll(List.of());
    }

    @Test
    void 발표_시각_전에는_처리하지_않는다() {
        // given
        ProjectMatchingRound round = round(Instant.now().plusSeconds(3600), true);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isFalse();
        assertThat(round.getResultPublishedAt()).isNull();
        then(loadProjectMemberPort).should(never()).listPendingByMatchingRoundId(any());
        then(saveProjectApplicationPort).should(never()).saveAll(any());
    }

    @Test
    void 자동_선발이_완료되지_않으면_발표하지_않는다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), false);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isFalse();
        assertThat(round.getResultPublishedAt()).isNull();
        then(loadProjectMemberPort).should(never()).listPendingByMatchingRoundId(any());
        then(saveProjectApplicationPort).should(never()).saveAll(any());
    }

    @Test
    void 이미_발표한_차수는_다시_처리하지_않는다() {
        // given
        ProjectMatchingRound round = round(Instant.now().minusSeconds(60), true);
        round.publishResult(Instant.now());
        Instant publishedAt = round.getResultPublishedAt();
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isFalse();
        assertThat(round.getResultPublishedAt()).isEqualTo(publishedAt);
        then(loadProjectMemberPort).should(never()).listPendingByMatchingRoundId(any());
        then(saveProjectApplicationPort).should(never()).saveAll(any());
    }

    @Test
    void 삭제된_차수는_발표하지_않는다() {
        // given
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID))
            .willThrow(new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FOUND));

        // when
        boolean published = sut.publish(ROUND_ID);

        // then
        assertThat(published).isFalse();
        then(loadProjectMemberPort).should(never()).listPendingByMatchingRoundId(any());
        then(saveProjectApplicationPort).should(never()).saveAll(any());
    }

    private Project project() {
        Project project = Project.createDraft(1L, 1L, EXECUTOR_ID, 1L, EXECUTOR_ID);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        return project;
    }

    private ProjectMatchingRound round(Instant publishAt, boolean autoDecided) {
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST, 1L, publishAt.minusSeconds(14400), publishAt.minusSeconds(7200),
            publishAt.minusSeconds(3600), publishAt);
        if (autoDecided) {
            round.executeAutoDecision(EXECUTOR_ID);
        }
        return round;
    }

    private ProjectApplication application(Project project, ProjectMatchingRound round, Long memberId,
        ProjectApplicationStatus status) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project, EXECUTOR_ID);
        ProjectApplication application = ProjectApplication.create(form, memberId * 10, memberId, round);
        ReflectionTestUtils.setField(application, "id", memberId);
        ReflectionTestUtils.setField(application, "status", status);
        return application;
    }
}
