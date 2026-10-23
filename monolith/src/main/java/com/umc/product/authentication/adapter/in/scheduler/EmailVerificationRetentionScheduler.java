package com.umc.product.authentication.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authentication.application.port.out.DeleteEmailVerificationPort;
import com.umc.product.global.logging.OperationalMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private static final String JOB_NAME = "email_verification_retention";

    private final DeleteEmailVerificationPort deleteEmailVerificationPort;
    private final OperationalMetrics operationalMetrics;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void purge() {
        Instant startedAt = Instant.now();
        Instant threshold = Instant.now().minus(RETENTION);
        try {
            int deleted = deleteEmailVerificationPort.deleteExpiredBefore(threshold);
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "success", duration, deleted);
            if (deleted > 0) {
                log.info("batch job completed: jobName={}, threshold={}, processed={}, durationMs={}, result={}",
                    JOB_NAME, threshold, deleted, duration.toMillis(), "success");
            } else {
                log.debug("batch job completed: jobName={}, threshold={}, processed={}, durationMs={}, result={}",
                    JOB_NAME, threshold, deleted, duration.toMillis(), "success");
            }
        } catch (RuntimeException e) {
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "failure", duration, 0);
            log.error("batch job failed: jobName={}, threshold={}, durationMs={}, result={}, errorClass={}",
                JOB_NAME, threshold, duration.toMillis(), "failure", e.getClass().getSimpleName(), e);
            throw e;
        }
    }
}
