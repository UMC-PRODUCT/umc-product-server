package com.umc.product.project.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MatchingRoundDeadlineRegistryTest {

    @Mock
    TaskScheduler taskScheduler;
    @Mock
    AutoDecideProjectMatchingRoundUseCase autoDecideUseCase;
    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    ScheduledFuture<?> scheduledFuture;
    @Mock
    ScheduledFuture<?> scheduledFuture2;

    MatchingRoundDeadlineRegistry sut;

    @BeforeEach
    void setUp() {
        sut = new MatchingRoundDeadlineRegistry(
            taskScheduler, autoDecideUseCase, loadProjectMatchingRoundPort
        );
    }

    @Nested
    class schedule {

        @Test
        void deadline_시점에_task가_등록된다() {
            ProjectMatchingRound round = roundWithId(1L);
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture);

            sut.schedule(round);

            assertThat(sut.isScheduled(1L)).isTrue();
            then(taskScheduler).should().schedule(any(Runnable.class), eq(round.getDecisionDeadline()));
        }

        @Test
        void 동일_id_재등록시_기존_task는_취소되고_새_task가_등록된다() {
            ProjectMatchingRound round = roundWithId(1L);
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture)
                .willAnswer(invocation -> scheduledFuture2);

            sut.schedule(round);
            sut.schedule(round);

            then(scheduledFuture).should().cancel(false);
            then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        void 등록된_task가_실행되면_autoDecide가_null_executor로_호출된다() {
            ProjectMatchingRound round = roundWithId(1L);
            ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
            given(taskScheduler.schedule(taskCaptor.capture(), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture);

            sut.schedule(round);
            taskCaptor.getValue().run();

            then(autoDecideUseCase).should().autoDecide(1L, null);
        }

        @Test
        void task_실행_중_예외가_발생해도_swallow되고_등록은_제거된다() {
            ProjectMatchingRound round = roundWithId(1L);
            ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
            given(taskScheduler.schedule(taskCaptor.capture(), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture);
            org.mockito.Mockito.doThrow(new RuntimeException("boom"))
                .when(autoDecideUseCase).autoDecide(any(), any());

            sut.schedule(round);
            taskCaptor.getValue().run();

            assertThat(sut.isScheduled(1L)).isFalse();
        }
    }

    @Nested
    class cancel {

        @Test
        void 등록되지_않은_id면_no_op() {
            sut.cancel(999L);

            then(scheduledFuture).should(never()).cancel(any(boolean.class));
        }

        @Test
        void 등록된_id면_task_취소하고_제거된다() {
            ProjectMatchingRound round = roundWithId(1L);
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture);
            sut.schedule(round);

            sut.cancel(1L);

            then(scheduledFuture).should().cancel(false);
            assertThat(sut.isScheduled(1L)).isFalse();
        }
    }

    @Nested
    class recoverPendingTasks {

        @Test
        void 부팅_시_미처리_round_모두_재등록된다() {
            ProjectMatchingRound round1 = roundWithId(1L);
            ProjectMatchingRound round2 = roundWithId(2L);
            given(loadProjectMatchingRoundPort.listAllNotAutoDecided())
                .willReturn(List.of(round1, round2));
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .willAnswer(invocation -> scheduledFuture);

            sut.recoverPendingTasks();

            assertThat(sut.isScheduled(1L)).isTrue();
            assertThat(sut.isScheduled(2L)).isTrue();
            then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        void 미처리_round가_없으면_등록되는_task도_없다() {
            given(loadProjectMatchingRoundPort.listAllNotAutoDecided()).willReturn(List.of());

            sut.recoverPendingTasks();

            then(taskScheduler).should(never()).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    private ProjectMatchingRound roundWithId(Long id) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "테스트 매칭 " + id, null,
            MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            Instant.now().plusSeconds(86_400),
            Instant.now().plusSeconds(172_800),
            Instant.now().plusSeconds(259_200)
        );
        ReflectionTestUtils.setField(round, "id", id);
        return round;
    }
}
