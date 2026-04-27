package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import java.util.List;

public interface LoadOriginalWorkbookMissionPort {

    List<OriginalWorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}