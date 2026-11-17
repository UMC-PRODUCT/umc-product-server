package com.umc.product.challenger.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.in.command.PurgeExpiredChallengerDeactivationsUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.global.logging.OperationalMetrics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "app.challenger.deactivation-retention",
    name = "enabled",
    havingValue = "true"
)
public class ChallengerDeactivationRetentionScheduler {

    private static final String JOB_NAME = "challenger_deactivation_retention";

    private final PurgeExpiredChallengerDeactivationsUseCase purgeExpiredChallengerDeactivationsUseCase;
    private final OperationalMetrics operationalMetrics;
    private final Executor executor;

    public ChallengerDeactivationRetentionScheduler(
        PurgeExpiredChallengerDeactivationsUseCase purgeExpiredChallengerDeactivationsUseCase,
        OperationalMetrics operationalMetrics,
        @Qualifier("challengerDeactivationRetentionExecutor") Executor executor
    ) {
        this.purgeExpiredChallengerDeactivationsUseCase = purgeExpiredChallengerDeactivationsUseCase;
        this.operationalMetrics = operationalMetrics;
        this.executor = executor;
    }

    @Scheduled(
        cron = "${app.challenger.deactivation-retention.cron}",
        zone = "Asia/Seoul"
    )
    public void purge() {
        try {
            executor.execute(this::executePurge);
        } catch (RejectedExecutionException exception) {
            log.warn("batch job execution skipped because previous run is still active: jobName={}", JOB_NAME);
        }
    }

    void executePurge() {
        Instant startedAt = Instant.now();
        try {
            ChallengerDeactivationPurgeResult result = purgeExpiredChallengerDeactivationsUseCase.purgeExpired();
            Duration duration = Duration.between(startedAt, Instant.now());
            String outcome = result.failedCount() == 0 ? "success" : "partial_failure";
            operationalMetrics.recordBatchJob(JOB_NAME, outcome, duration, result.deletedCount());
            log.info(
                "batch job completed: jobName={}, deleted={}, failed={}, durationMs={}, result={}",
                JOB_NAME,
                result.deletedCount(),
                result.failedCount(),
                duration.toMillis(),
                outcome
            );
        } catch (RuntimeException exception) {
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "failure", duration, 0);
            log.error(
                "batch job failed: jobName={}, durationMs={}, result={}, errorClass={}",
                JOB_NAME,
                duration.toMillis(),
                "failure",
                exception.getClass().getSimpleName(),
                exception
            );
            throw exception;
        }
    }
}
