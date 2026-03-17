package com.umc.product.curriculum.adapter.in.scheduler;

import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final AutoReleaseWorkbookUseCase autoReleaseWorkbookUseCase;

    /**
     * 매일 자정(KST)에 실행
     * <p>
     * - 크론 표현식 : 초 분 시 일 월 요일 "0 0 0 * * *" = 매일 00:00:00 - 테스트용 표현식 : "0 * * * * *" = 매 1분마다 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void releaseDueWorkbooks() {
        log.info("[WorkbookAutoRelease] 자동 배포 스케줄 시작");

        try {
            int releasedCount = autoReleaseWorkbookUseCase.releaseAllDue();
            log.info("[WorkbookAutoRelease] 자동 배포 스케줄 완료: {}건 배포", releasedCount);
        } catch (Exception e) {
            log.error("[WorkbookAutoRelease] 자동 배포 중 오류 발생", e);
        }
    }
}
