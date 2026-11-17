package com.umc.product.challenger.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.umc.product.challenger.application.port.in.command.PurgeExpiredChallengerDeactivationsUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.global.logging.OperationalMetrics;

@ExtendWith(MockitoExtension.class)
class ChallengerDeactivationRetentionSchedulerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(PurgeExpiredChallengerDeactivationsUseCase.class, () ->
            org.mockito.Mockito.mock(PurgeExpiredChallengerDeactivationsUseCase.class))
        .withBean(OperationalMetrics.class, () -> org.mockito.Mockito.mock(OperationalMetrics.class))
        .withUserConfiguration(
            ChallengerDeactivationRetentionScheduler.class,
            ChallengerDeactivationRetentionSchedulerConfig.class
        );

    @Mock
    PurgeExpiredChallengerDeactivationsUseCase purgeExpiredChallengerDeactivationsUseCase;

    @Mock
    OperationalMetrics operationalMetrics;

    @Mock
    Executor executor;

    @InjectMocks
    ChallengerDeactivationRetentionScheduler scheduler;

    @Test
    @DisplayName("보관 만료 기능이 비활성화되면 스케줄러 빈을 등록하지 않는다")
    void 보관_만료_기능_비활성화() {
        contextRunner
            .withPropertyValues("app.challenger.deactivation-retention.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(ChallengerDeactivationRetentionScheduler.class));
    }

    @Test
    @DisplayName("보관 만료 기능이 활성화되면 스케줄러 빈을 등록한다")
    void 보관_만료_기능_활성화() {
        contextRunner
            .withPropertyValues("app.challenger.deactivation-retention.enabled=true")
            .run(context -> assertThat(context)
                .hasSingleBean(ChallengerDeactivationRetentionScheduler.class));
    }

    @Test
    @DisplayName("스케줄러는 만료 삭제 작업을 전용 실행기에 제출한다")
    void 만료_삭제_작업_제출() {
        // when
        scheduler.purge();

        // then
        then(executor).should().execute(any(Runnable.class));
        then(purgeExpiredChallengerDeactivationsUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이전 만료 삭제 작업이 실행 중이면 새 실행 요청을 건너뛴다")
    void 중복_실행_요청_거절() {
        // given
        org.mockito.BDDMockito.willThrow(new RejectedExecutionException()).given(executor)
            .execute(any(Runnable.class));

        // when
        scheduler.purge();

        // then
        then(purgeExpiredChallengerDeactivationsUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("만료 기록을 모두 삭제하면 성공 지표를 기록한다")
    void 만료_기록_삭제_성공() {
        // given
        given(purgeExpiredChallengerDeactivationsUseCase.purgeExpired())
            .willReturn(new ChallengerDeactivationPurgeResult(3, 0));

        // when
        scheduler.executePurge();

        // then
        then(operationalMetrics).should().recordBatchJob(
            eq("challenger_deactivation_retention"),
            eq("success"),
            any(Duration.class),
            eq(3L)
        );
    }

    @Test
    @DisplayName("일부 만료 기록 삭제에 실패하면 부분 실패 지표를 기록한다")
    void 만료_기록_일부_삭제_실패() {
        // given
        given(purgeExpiredChallengerDeactivationsUseCase.purgeExpired())
            .willReturn(new ChallengerDeactivationPurgeResult(2, 1));

        // when
        scheduler.executePurge();

        // then
        then(operationalMetrics).should().recordBatchJob(
            eq("challenger_deactivation_retention"),
            eq("partial_failure"),
            any(Duration.class),
            eq(2L)
        );
    }

    @Test
    @DisplayName("보관 만료 처리 자체가 실패하면 실패 지표를 기록하고 예외를 전파한다")
    void 보관_만료_처리_실패() {
        // given
        RuntimeException exception = new RuntimeException("purge failed");
        given(purgeExpiredChallengerDeactivationsUseCase.purgeExpired()).willThrow(exception);

        // when & then
        assertThatThrownBy(scheduler::executePurge).isSameAs(exception);
        then(operationalMetrics).should().recordBatchJob(
            eq("challenger_deactivation_retention"),
            eq("failure"),
            any(Duration.class),
            eq(0L)
        );
        then(operationalMetrics).should(never()).recordBatchJob(
            eq("challenger_deactivation_retention"),
            eq("success"),
            any(Duration.class),
            eq(0L)
        );
    }
}
