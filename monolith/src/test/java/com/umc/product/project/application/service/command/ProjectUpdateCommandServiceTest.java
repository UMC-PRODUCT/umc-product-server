package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectUpdateCommandServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    @Mock LoadProjectPort loadProjectPort;
    @Mock LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock CheckPermissionUseCase checkPermissionUseCase;
    @Mock Clock clock;
    @InjectMocks ProjectUpdateCommandService sut;

    @BeforeEach
    void 현재시각을_고정한다() {
        org.mockito.Mockito.lenient().when(clock.instant()).thenReturn(NOW);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"DRAFT", "PENDING_REVIEW", "IN_PROGRESS"})
    void 지원기간이_아니면_기존_수정가능_상태에서_부분_수정한다(ProjectStatus status) {
        // given
        Project project = givenProject(status);

        // when
        assertThat(sut.update(command())).isEqualTo(status);

        // then
        assertThat(project.getName()).isEqualTo("새 이름");
        assertThat(project.getDescription()).isEqualTo("기존 설명");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"COMPLETED", "ABORTED"})
    void 지원기간_밖이어도_종료_상태는_수정할_수_없다(ProjectStatus status) {
        // given
        Project project = givenProject(status);

        // when & then
        assertDenied(ProjectErrorCode.PROJECT_INVALID_STATE);
        assertThat(project.getName()).isEqualTo("기존 이름");
    }

    @ParameterizedTest
    @EnumSource(value = MatchingPhase.class, names = {"FIRST", "SECOND", "THIRD"})
    void 각_차수의_지원기간이면_어느_정보도_변경하지_않는다(MatchingPhase phase) {
        // given
        Project project = givenProject(ProjectStatus.IN_PROGRESS);
        given(loadProjectMatchingRoundPort.listByChapterIdForUpdate(2L))
            .willReturn(List.of(round(phase, -60, 60)));

        // when & then
        assertDenied(ProjectErrorCode.PROJECT_UPDATE_DURING_APPLICATION_PERIOD);
        assertThat(project.getName()).isEqualTo("기존 이름");
        assertThat(project.getDescription()).isEqualTo("기존 설명");
    }

    @ParameterizedTest
    @MethodSource("지원기간_경계")
    void 지원기간_시작과_마감_시각을_포함한다(long startsIn, long endsIn, boolean blocked) {
        // given
        Project project = givenProject(ProjectStatus.IN_PROGRESS);
        given(loadProjectMatchingRoundPort.listByChapterIdForUpdate(2L))
            .willReturn(List.of(round(MatchingPhase.FIRST, startsIn, endsIn)));

        // when & then
        if (blocked) {
            assertDenied(ProjectErrorCode.PROJECT_UPDATE_DURING_APPLICATION_PERIOD);
            assertThat(project.getName()).isEqualTo("기존 이름");
        } else {
            sut.update(command());
            assertThat(project.getName()).isEqualTo("새 이름");
        }
    }

    static Stream<Arguments> 지원기간_경계() {
        return Stream.of(Arguments.of(1L, 60L, false), Arguments.of(0L, 60L, true),
            Arguments.of(-60L, 0L, true), Arguments.of(-60L, -1L, false));
    }

    @Test
    void 판정기간과_랜덤매칭_중에는_수정할_수_있다() {
        // given
        Project project = givenProject(ProjectStatus.IN_PROGRESS);
        given(loadProjectMatchingRoundPort.listByChapterIdForUpdate(2L)).willReturn(List.of(
            round(MatchingPhase.FIRST, -120, -60), round(MatchingPhase.RANDOM_MATCHING, -60, 60)));

        // when
        sut.update(command());

        // then
        assertThat(project.getName()).isEqualTo("새 이름");
    }

    @Test
    void 내부_호출도_수정_권한을_검사한다() {
        // given
        Project project = Project.createDraft(1L, 2L, 100L, 3L, 100L);
        ReflectionTestUtils.setField(project, "id", 1L);
        given(loadProjectPort.getById(1L)).willReturn(project);
        doThrow(new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED))
            .when(checkPermissionUseCase).checkOrThrow(eq(100L), any());

        // when & then
        assertDenied(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        verify(loadProjectMatchingRoundPort, never()).listByChapterIdForUpdate(any());
        verify(loadProjectPort, never()).getByIdForUpdate(any());
    }

    private Project givenProject(ProjectStatus status) {
        Project project = Project.createDraft(1L, 2L, 100L, 3L, 100L);
        ReflectionTestUtils.setField(project, "id", 1L);
        project.updateBasicInfo("기존 이름", "기존 설명", null, null, null);
        ReflectionTestUtils.setField(project, "status", status);
        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectPort.getByIdForUpdate(1L)).willReturn(project);
        return project;
    }

    private ProjectMatchingRound round(MatchingPhase phase, long startsIn, long endsIn) {
        Instant endsAt = NOW.plusSeconds(endsIn);
        Instant deadline = phase == MatchingPhase.RANDOM_MATCHING ? endsAt : NOW.plusSeconds(120);
        return ProjectMatchingRound.create("수정 기간 검증", null, MatchingType.PLAN_DESIGN, phase,
            2L, NOW.plusSeconds(startsIn), endsAt, deadline, deadline.plusSeconds(600));
    }

    private UpdateProjectCommand command() {
        return UpdateProjectCommand.builder().projectId(1L).requesterMemberId(100L).name("새 이름").build();
    }

    private void assertDenied(ProjectErrorCode code) {
        assertThatThrownBy(() -> sut.update(command())).isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(code);
    }
}
