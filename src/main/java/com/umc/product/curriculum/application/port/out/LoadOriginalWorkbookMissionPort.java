package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import java.util.List;

public interface LoadOriginalWorkbookMissionPort {

    List<OriginalWorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);

    /**
     * 여러 원본 워크북에 속한 미션 일괄 조회 (N+1 방지)
     */
    List<OriginalWorkbookMission> findByOriginalWorkbookIdIn(List<Long> originalWorkbookIds);
}