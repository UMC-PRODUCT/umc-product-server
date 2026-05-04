package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.time.Instant;
import java.util.List;

public interface LoadOriginalWorkbookPort {

    OriginalWorkbook getById(Long id);

    /**
     * 배치 조회 — IDs 중 하나라도 없으면 예외 던짐
     */
    List<OriginalWorkbook> batchGetByIds(List<Long> ids);

    /**
     * 해당 주차별 커리큘럼에 배포(RELEASED)된 원본 워크북 목록 조회
     */
    List<OriginalWorkbook> findReleasedByWeeklyCurriculumId(Long weeklyCurriculumId);

    /**
     * 여러 주차별 커리큘럼에 배포(RELEASED)된 원본 워크북 목록 일괄 조회 (N+1 방지)
     */
    List<OriginalWorkbook> findReleasedByWeeklyCurriculumIdIn(List<Long> weeklyCurriculumIds);

    /**
     * 미배포 상태이면서 시작일이 지난 워크북 목록 조회 (자동 배포 대상)
     */
    List<OriginalWorkbook> findUnreleasedWithStartDateBefore(Instant now);
}
