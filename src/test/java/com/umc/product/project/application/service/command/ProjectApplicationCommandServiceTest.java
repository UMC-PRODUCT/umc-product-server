package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.project.application.port.in.command.dto.CancelProjectApplicationCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationCommandServiceTest {

    private static final Long APPLICATION_ID = 500L;
    private static final Long APPLICANT_MEMBER_ID = 100L;

    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    SaveProjectApplicationPort saveProjectApplicationPort;
    @Mock
    LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    ManageFormResponseUseCase manageFormResponseUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @InjectMocks
    ProjectApplicationCommandService sut;

    @Test
    void cancel_DRAFT를_CANCELLED로_전이하고_저장한다() {
        ProjectApplication application = application(ProjectApplicationStatus.DRAFT, roundOpen());
        given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
        given(saveProjectApplicationPort.save(any(ProjectApplication.class))).willAnswer(inv -> inv.getArgument(0));

        ProjectApplicationInfo result = sut.cancel(cancelCommand("마음 바뀜"));

        assertThat(result.status()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        assertThat(application.getStatusChangedMemberId()).isEqualTo(APPLICANT_MEMBER_ID);
        assertThat(application.getStatusChangeReason()).isEqualTo("마음 바뀜");
        verify(saveProjectApplicationPort).save(application);
    }

    @Test
    void cancel_SUBMITTED를_CANCELLED로_전이하고_저장한다() {
        ProjectApplication application = application(ProjectApplicationStatus.SUBMITTED, roundOpen());
        given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
        given(saveProjectApplicationPort.save(any(ProjectApplication.class))).willAnswer(inv -> inv.getArgument(0));

        ProjectApplicationInfo result = sut.cancel(cancelCommand(null));

        assertThat(result.status()).isEqualTo(ProjectApplicationStatus.CANCELLED);
        verify(saveProjectApplicationPort).save(application);
    }

    @Test
    void cancel_차수가_종료되었으면_CANCEL_ROUND_CLOSED() {
        ProjectApplication application = application(ProjectApplicationStatus.SUBMITTED, roundClosed());
        given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> sut.cancel(cancelCommand("늦었지만")))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_ROUND_CLOSED);

        verify(saveProjectApplicationPort, never()).save(any(ProjectApplication.class));
    }

    @Test
    void cancel_지원서가_없으면_PROJECT_APPLICATION_NOT_FOUND() {
        given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.cancel(cancelCommand(null)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);

        verify(saveProjectApplicationPort, never()).save(any(ProjectApplication.class));
    }

    @Test
    void cancel_이미_CANCELLED라면_도메인이_CANCEL_NOT_ALLOWED() {
        ProjectApplication application = application(ProjectApplicationStatus.CANCELLED, roundOpen());
        given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> sut.cancel(cancelCommand(null)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_NOT_ALLOWED);

        verify(saveProjectApplicationPort, never()).save(any(ProjectApplication.class));
    }

    private CancelProjectApplicationCommand cancelCommand(String reason) {
        return CancelProjectApplicationCommand.builder()
            .applicationId(APPLICATION_ID)
            .requesterMemberId(APPLICANT_MEMBER_ID)
            .reason(reason)
            .build();
    }

    private ProjectApplication application(ProjectApplicationStatus status, ProjectMatchingRound round) {
        try {
            var constructor = ProjectApplication.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ProjectApplication application = constructor.newInstance();
            ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
            ReflectionTestUtils.setField(application, "applicantMemberId", APPLICANT_MEMBER_ID);
            ReflectionTestUtils.setField(application, "status", status);
            ReflectionTestUtils.setField(application, "appliedMatchingRound", round);
            return application;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectMatchingRound roundOpen() {
        Instant now = Instant.now();
        return round(now.minusSeconds(60), now.plusSeconds(3600));
    }

    private ProjectMatchingRound roundClosed() {
        Instant now = Instant.now();
        return round(now.minusSeconds(7200), now.minusSeconds(3600));
    }

    private ProjectMatchingRound round(Instant startsAt, Instant endsAt) {
        try {
            var constructor = ProjectMatchingRound.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ProjectMatchingRound round = constructor.newInstance();
            ReflectionTestUtils.setField(round, "id", 700L);
            ReflectionTestUtils.setField(round, "startsAt", startsAt);
            ReflectionTestUtils.setField(round, "endsAt", endsAt);
            return round;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
