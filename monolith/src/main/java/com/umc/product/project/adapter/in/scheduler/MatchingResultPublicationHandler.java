package com.umc.product.project.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.project.application.port.in.command.PublishProjectMatchingResultUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매칭 결과 발표 시점에 공개 UseCase 를 호출하는 driving adapter.
 * <p>
 * out-side {@link com.umc.product.project.adapter.out.scheduler.MatchingRoundDeadlineScheduler}
 * 가 등록한 1회성 task 에서 호출합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingResultPublicationHandler {

    private static final String JOB_NAME = "matching_result_publication";

    private final PublishProjectMatchingResultUseCase publishProjectMatchingResultUseCase;
    private final OperationalMetrics operationalMetrics;

    /**
     * 발표 시점에 1회 호출되어 결과를 공개한다. 예외는 기록하고 종료하여 다음 task 진행을 막지 않는다.
     */
    public void handle(Long matchingRoundId) {
        Instant startedAt = Instant.now();
        try {
            publishProjectMatchingResultUseCase.publish(matchingRoundId);
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "success", duration, 1);
        } catch (Exception e) {
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "failure", duration, 0);
            log.error("batch job failed: jobName={}, matchingRoundId={}, durationMs={}, result={}, errorClass={}",
                JOB_NAME, matchingRoundId, duration.toMillis(), "failure", e.getClass().getSimpleName(), e);
        }
    }
}
