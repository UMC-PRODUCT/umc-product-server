package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginalWorkbookPersistenceAdapter implements LoadOriginalWorkbookPort, SaveOriginalWorkbookPort {

    private final OriginalWorkbookJpaRepository originalWorkbookJpaRepository;
    private final CurriculumQueryRepository curriculumQueryRepository;

    @Override
    public OriginalWorkbook findById(Long id) {
        return originalWorkbookJpaRepository.findById(id)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_NOT_FOUND));
    }

    @Override
    public List<OriginalWorkbook> findReleasedByWeeklyCurriculumIdIn(List<Long> weeklyCurriculumIds) {
        if (weeklyCurriculumIds.isEmpty()) {
            return List.of();
        }
        return originalWorkbookJpaRepository.findByWeeklyCurriculumIdInAndOriginalWorkbookStatus(
            weeklyCurriculumIds, OriginalWorkbookStatus.RELEASED);
    }

    @Override
    public List<OriginalWorkbook> findUnreleasedWithStartDateBefore(Instant now) {
        return curriculumQueryRepository.findUnreleasedWorkbookIdsWithStartDateBefore(now);
    }

    @Override
    public OriginalWorkbook save(OriginalWorkbook workbook) {
        return originalWorkbookJpaRepository.save(workbook);
    }
}