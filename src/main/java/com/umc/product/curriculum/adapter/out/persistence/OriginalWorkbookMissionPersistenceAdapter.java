package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookMissionPort;
import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginalWorkbookMissionPersistenceAdapter implements LoadOriginalWorkbookMissionPort {

    private final OriginalWorkbookMissionJpaRepository originalWorkbookMissionJpaRepository;

    @Override
    public List<OriginalWorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId) {
        return originalWorkbookMissionJpaRepository.findByOriginalWorkbookId(originalWorkbookId);
    }
}