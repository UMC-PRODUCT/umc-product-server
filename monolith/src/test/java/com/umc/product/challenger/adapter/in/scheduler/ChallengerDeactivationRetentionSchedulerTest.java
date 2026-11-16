package com.umc.product.challenger.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.command.PurgeExpiredChallengerDeactivationsUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.global.logging.OperationalMetrics;

@ExtendWith(MockitoExtension.class)
class ChallengerDeactivationRetentionSchedulerTest {

    @Mock
    PurgeExpiredChallengerDeactivationsUseCase purgeExpiredChallengerDeactivationsUseCase;

    @Mock
    OperationalMetrics operationalMetrics;

    @InjectMocks
    ChallengerDeactivationRetentionScheduler scheduler;

    @Test
    @DisplayName("만료 기록을 모두 삭제하면 성공 지표를 기록한다")
    void 만료_기록_삭제_성공() {
        // given
        given(purgeExpiredChallengerDeactivationsUseCase.purgeExpired())
            .willReturn(new ChallengerDeactivationPurgeResult(3, 0));

        // when
        scheduler.purge();

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
        scheduler.purge();

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
        assertThatThrownBy(scheduler::purge).isSameAs(exception);
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
