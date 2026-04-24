package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.NotImplementedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerWorkbookPersistenceAdapter implements LoadChallengerWorkbookPort, SaveChallengerWorkbookPort {

    private final ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;

    @Override
    public ChallengerWorkbook findById(Long id) {
        return challengerWorkbookJpaRepository.findById(id)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.CHALLENGER_WORKBOOK_NOT_FOUND));
    }

    @Override
    public List<ChallengerWorkbook> findAllByChallengerIdAndOriginalWorkbookId(Long challengerId,
                                                                               Long originalWorkbookId) {
        throw new NotImplementedException();
//        return challengerWorkbookJpaRepository.findAllByChallengerIdAndOriginalWorkbookIdOrderByIdDesc(
//            challengerId,
//            originalWorkbookId
//        );
    }

    @Override
    public List<Long> findOriginalWorkbookIdsWithSubmissions(List<Long> originalWorkbookIds) {
        if (originalWorkbookIds.isEmpty()) {
            return List.of();
        }

        throw new NotImplementedException();
//        return challengerWorkbookJpaRepository.findOriginalWorkbookIdsByOriginalWorkbookIdIn(originalWorkbookIds);
    }

    @Override
    public ChallengerWorkbook save(ChallengerWorkbook challengerWorkbook) {
        return challengerWorkbookJpaRepository.save(challengerWorkbook);
    }
}
