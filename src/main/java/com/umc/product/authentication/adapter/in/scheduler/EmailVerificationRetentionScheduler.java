package com.umc.product.authentication.adapter.in.scheduler;

import com.umc.product.authentication.application.port.out.DeleteEmailVerificationPort;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * email_verification 의 만료된 세션을 주기적으로 정리하는 회수 잡.
 * <p>
 * 만료 직후 즉시 삭제하지 않고 일정 기간(retention) 을 두어 운영 디버깅 가능 시점을 확보한다.
 * 기본값은 매일 03:00 (Asia/Seoul), retention 7 일.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationRetentionScheduler {

    private static final Duration RETENTION = Duration.ofDays(7);

    private final DeleteEmailVerificationPort deleteEmailVerificationPort;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void purge() {
        Instant threshold = Instant.now().minus(RETENTION);
        int deleted = deleteEmailVerificationPort.deleteExpiredBefore(threshold);
        if (deleted > 0) {
            log.info("email_verification 회수 완료: threshold={}, deleted={}", threshold, deleted);
        } else {
            log.debug("email_verification 회수: 보존 기간 초과 행 없음. threshold={}", threshold);
        }
    }
}
