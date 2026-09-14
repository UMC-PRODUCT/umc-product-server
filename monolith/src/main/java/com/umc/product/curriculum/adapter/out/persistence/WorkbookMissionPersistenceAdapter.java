package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadWorkbookMissionPort;
import com.umc.product.curriculum.domain.WorkbookMission;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkbookMissionPersistenceAdapter implements LoadWorkbookMissionPort {

    private final WorkbookMissionJpaRepository workbookMissionJpaRepository;

    @Override
    public Optional<WorkbookMission> findById(Long id) {
        return workbookMissionJpaRepository.findById(id);
    }

    @Override
    public List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId) {
        return workbookMissionJpaRepository.findByOriginalWorkbookId(originalWorkbookId);
    }
}
