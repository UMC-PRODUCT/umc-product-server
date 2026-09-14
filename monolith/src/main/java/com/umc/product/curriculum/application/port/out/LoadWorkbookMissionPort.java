package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.WorkbookMission;
import java.util.List;
import java.util.Optional;

public interface LoadWorkbookMissionPort {

    Optional<WorkbookMission> findById(Long id);

    List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
