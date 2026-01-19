package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginalWorkbookPersistenceAdapter implements LoadOriginalWorkbookPort {

    private final OriginalWorkbookJpaRepository originalWorkbookJpaRepository;

    @Override
    public Optional<OriginalWorkbook> findById(Long id) {
        return originalWorkbookJpaRepository.findById(id);
    }

    @Override
    public List<OriginalWorkbook> findByCurriculumId(Long curriculumId) {
        return originalWorkbookJpaRepository.findByCurriculumId(curriculumId);
    }

    @Override
    public List<OriginalWorkbook> findByCurriculumIdOrderByWeekNo(Long curriculumId) {
        return originalWorkbookJpaRepository.findByCurriculumIdOrderByWeekNoAsc(curriculumId);
    }

    @Override
    public List<Integer> findDistinctWeekNoByGisuId(Long gisuId) {
        return originalWorkbookJpaRepository.findDistinctWeekNoByGisuId(gisuId);
    }
}
