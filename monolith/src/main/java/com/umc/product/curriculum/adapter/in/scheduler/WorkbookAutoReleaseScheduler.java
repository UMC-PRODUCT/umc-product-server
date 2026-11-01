package com.umc.product.curriculum.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
import com.umc.product.global.logging.OperationalMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 워크북 자동 배포 스케줄러
 * <p>
 * 매일 자정(KST)에 실행되어 배포 조건을 만족하는 워크북을 자동 배포합니다.
 * <p>
 * 배포 조건:
 * <ul>
 *   <li>startDate가 현재 시간보다 과거</li>
 *   <li>아직 배포되지 않음 (releasedAt == null)</li>
 *   <li>직전 주차(weekNo - 1)가 배포됨 (1주차는 직전 주차 없으므로 스킵)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkbookAutoReleaseScheduler {

    private static final String JOB_NAME = "workbook_auto_release";

    private final AutoReleaseWorkbookUseCase autoReleaseWorkbookUseCase;
    private final OperationalMetrics operationalMetrics;

    /**
     * 매일 자정(KST)에 실행
     * <p>
     * - 크론 표현식 : 초 분 시 일 월 요일 "0 0 0 * * *" = 매일 00:00:00 - 테스트용 표현식 : "0 * * * * *" = 매 1분마다 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void releaseDueWorkbooks() {
        Instant startedAt = Instant.now();
        log.info("batch job started: jobName={}", JOB_NAME);

        try {
            int releasedCount = autoReleaseWorkbookUseCase.releaseAllDue();
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "success", duration, releasedCount);
            log.info("batch job completed: jobName={}, processed={}, durationMs={}, result={}",
                JOB_NAME, releasedCount, duration.toMillis(), "success");
        } catch (Exception e) {
            Duration duration = Duration.between(startedAt, Instant.now());
            operationalMetrics.recordBatchJob(JOB_NAME, "failure", duration, 0);
            log.error("batch job failed: jobName={}, durationMs={}, result={}, errorClass={}",
                JOB_NAME, duration.toMillis(), "failure", e.getClass().getSimpleName(), e);
        }
    }
}
