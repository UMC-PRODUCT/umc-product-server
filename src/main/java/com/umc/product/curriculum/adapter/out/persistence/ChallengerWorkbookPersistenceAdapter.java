package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerWorkbookPersistenceAdapter implements LoadChallengerWorkbookPort, SaveChallengerWorkbookPort {

    private final ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;

    @Override
    public ChallengerWorkbook findById(Long id) {
        return challengerWorkbookJpaRepository.findById(id).orElseThrow(() -> new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.CHALLENGER_WORKBOOK_NOT_FOUND));
    }

    @Override
    public List<Long> findOriginalWorkbookIdsWithSubmissions(List<Long> originalWorkbookIds) {
        if (originalWorkbookIds.isEmpty()) {
            return List.of();
        }
        return challengerWorkbookJpaRepository.findOriginalWorkbookIdsByOriginalWorkbookIdIn(originalWorkbookIds);
    }

    @Override
    public ChallengerWorkbook save(ChallengerWorkbook challengerWorkbook) {
        return challengerWorkbookJpaRepository.save(challengerWorkbook);
    }
}
