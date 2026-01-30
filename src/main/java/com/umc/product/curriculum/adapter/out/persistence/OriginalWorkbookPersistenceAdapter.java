package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
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
        return originalWorkbookJpaRepository.findById(id).orElseThrow(() -> new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.WORKBOOK_NOT_FOUND));
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

    @Override
    public List<CurriculumWeekInfo> findWeekInfoByActiveGisuAndPart(ChallengerPart part) {
        return curriculumQueryRepository.findWeekInfoByActiveGisuAndPart(part);
    }

    @Override
    public OriginalWorkbook save(OriginalWorkbook workbook) {
        return originalWorkbookJpaRepository.save(workbook);
    }
}
