package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import java.util.List;

public interface LoadOriginalWorkbookMissionPort {

    /**
     * 여러 원본 워크북에 속한 미션 일괄 조회
     */
    List<OriginalWorkbookMission> findByOriginalWorkbookIdIn(List<Long> originalWorkbookIds);
}