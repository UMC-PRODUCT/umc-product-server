package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.time.Instant;
import java.util.List;

public interface LoadOriginalWorkbookPort {

    OriginalWorkbook findById(Long id);

    /**
     * 여러 주차별 커리큘럼에 배포(RELEASED)된 원본 워크북 일괄 조회
     */
    List<OriginalWorkbook> findReleasedByWeeklyCurriculumIdIn(List<Long> weeklyCurriculumIds);

    /**
     * 미배포 상태이면서 시작일이 지난 워크북 목록 조회 (자동 배포 대상)
     */
    List<OriginalWorkbook> findUnreleasedWithStartDateBefore(Instant now);
}
