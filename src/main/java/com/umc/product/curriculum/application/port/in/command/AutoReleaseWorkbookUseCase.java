package com.umc.product.curriculum.application.port.in.command;

/**
 * 워크북 자동 배포 UseCase
 * <p>
 * 스케줄러에서 호출되어 배포 조건을 만족하는 워크북들을 일괄 배포합니다.
 * <p>
 * 배포 조건:
 * <ul>
 *   <li>startDate가 현재 시간보다 과거</li>
 *   <li>아직 배포되지 않음 (releasedAt == null)</li>
 *   <li>직전 주차(weekNo - 1)가 배포됨 (1주차는 직전 주차 없으므로 스킵)</li>
 * </ul>
 */
public interface AutoReleaseWorkbookUseCase {

    /**
     * 배포 조건을 만족하는 모든 워크북을 일괄 배포합니다.
     * <p>
     * 연쇄 배포 방지를 위해 조회를 먼저 완료한 후 배포를 수행합니다.
     *
     * @return 배포된 워크북 수
     */
//    int releaseAllDue();
}
