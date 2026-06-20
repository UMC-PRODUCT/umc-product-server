package com.umc.product.authentication.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authentication.application.port.out.DeleteEmailVerificationPort;
import com.umc.product.global.logging.OperationalMetrics;

/**
 * EmailVerificationRetentionScheduler 단위 테스트.
 * <p>
 * 매일 03:00 cron 발동 자체는 Spring 환경 책임이므로 단위 테스트에서는 purge() 메서드가
 * deletePort 에 적절한 threshold (현재 - retention) 를 위임하는지만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationRetentionSchedulerTest {

    @Mock
    DeleteEmailVerificationPort deleteEmailVerificationPort;

    @Mock
    OperationalMetrics operationalMetrics;

    @InjectMocks
    EmailVerificationRetentionScheduler scheduler;

    @Test
    @DisplayName("purge 호출 시 현재 - 7일 이전 만료 레코드 삭제를 위임한다")
    void retention_threshold_위임() {
        // given
        Instant before = Instant.now();
        given(deleteEmailVerificationPort.deleteExpiredBefore(any(Instant.class))).willReturn(3);

        // when
        scheduler.purge();

        // then
        Instant after = Instant.now();
        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        then(deleteEmailVerificationPort).should().deleteExpiredBefore(captor.capture());
        Instant threshold = captor.getValue();
        // retention 7일 ± 약간의 호출 지연을 허용
        assertThat(threshold).isBetween(
            before.minus(Duration.ofDays(7)).minusSeconds(1),
            after.minus(Duration.ofDays(7)).plusSeconds(1)
        );
    }
}
