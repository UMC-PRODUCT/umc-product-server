package com.umc.product.curriculum.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.curriculum.domain.WorkbookMission;

public interface LoadWorkbookMissionPort {

    Optional<WorkbookMission> findById(Long id);

    List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
