package com.umc.product.figma.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.figma.adapter.out.external.FigmaSummaryProperties;
import com.umc.product.figma.application.port.out.SaveFigmaCommentDispatchPort;
import com.umc.product.global.logging.OperationalMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * figma_comment_dispatch 의 보존 기간 초과 행을 정리하는 회수 잡 (ADR-004 §Implementation Plan §7).
 * <p>
 * dispatch 테이블은 "이미 발송된 댓글" 의 가드 책임만 가지므로, 운영적으로 의미를 잃은 (보존 기간을 넘긴) 행을 주기적으로 삭제해 무한 누적을 막는다. 회수 주기는
 * {@code app.figma.summary.retention-poll-interval} (기본 24시간) 으로, 보존 기간은 {@code app.figma.summary.dispatch-retention}
 * (기본 90일) 로 설정한다.
 * <p>
 * 운영진이 이전 시간창을 명시적으로 force=true 로 재발송하려면, dispatch 가 존재하던 댓글이 회수된 시점부터는 force 옵션 없이도 재발송된다 (의도된 동작 — 보존 기간을 넘은 댓글은 더 이상
 * 신뢰할 dispatch 기록이 없다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.figma.sync.enabled", havingValue = "true")
public class FigmaCommentDispatchRetentionScheduler {

    private static final String JOB_NAME = "figma_comment_dispatch_retention";

    private final SaveFigmaCommentDispatchPort saveFigmaCommentDispatchPort;
    private final FigmaSummaryProperties figmaSummaryProperties;
    private final OperationalMetrics operationalMetrics;

    @Scheduled(fixedDelayString = "${app.figma.summary.retention-poll-interval}")
    public void purge() {
        Instant startedAt = Instant.now();
        Instant threshold = Instant.now().minus(figmaSummaryProperties.dispatchRetention());
        try {
            int deleted = saveFigmaCommentDispatchPort.deleteOlderThan(threshold);
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
