package com.umc.product.project.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.project.application.port.in.command.PublishProjectMatchingResultUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 스케줄러가 발표 시각에 호출하여 매칭 결과를 공개한다.
 * 처리 중 예외가 발생하면 로그와 실패 지표를 기록하고 종료한다. 자동 재시도는 하지 않는다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingResultPublicationHandler {

    private static final String JOB_NAME = "matching_result_publication";

    private final PublishProjectMatchingResultUseCase publishProjectMatchingResultUseCase;
    private final OperationalMetrics operationalMetrics;

    public void handle(Long matchingRoundId) {
        Instant startedAt = Instant.now();
        try {
            boolean published = publishProjectMatchingResultUseCase.publish(matchingRoundId);
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, published ? "success" : "skipped", duration, published ? 1 : 0);
        } catch (Exception e) {
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "failure", duration, 0);
            log.error("batch job failed: jobName={}, matchingRoundId={}, durationMs={}, result={}, errorClass={}",
                JOB_NAME, matchingRoundId, duration.toMillis(), "failure", e.getClass().getSimpleName(), e);
        }
    }
}
