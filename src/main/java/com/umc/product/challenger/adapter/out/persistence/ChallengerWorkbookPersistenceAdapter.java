package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerWorkbookPersistenceAdapter implements LoadChallengerWorkbookPort {

    private final ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;

    @Override
    public Optional<ChallengerWorkbook> findById(Long id) {
        return challengerWorkbookJpaRepository.findById(id);
    }

    @Override
    public Optional<ChallengerWorkbook> findByChallengerIdAndOriginalWorkbookId(Long challengerId, Long originalWorkbookId) {
        return challengerWorkbookJpaRepository.findByChallengerIdAndOriginalWorkbookId(challengerId, originalWorkbookId);
    }

    @Override
    public List<ChallengerWorkbook> findByChallengerId(Long challengerId) {
        return challengerWorkbookJpaRepository.findByChallengerId(challengerId);
    }

    @Override
    public List<ChallengerWorkbook> findByChallengerIdAndCurriculumId(Long challengerId, Long curriculumId) {
        // TODO: QueryRepository 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ChallengerWorkbook> findByOriginalWorkbookId(Long originalWorkbookId) {
        return challengerWorkbookJpaRepository.findByOriginalWorkbookId(originalWorkbookId);
    }

    @Override
    public List<ChallengerWorkbook> findByOriginalWorkbookIdAndStudyGroupId(Long originalWorkbookId, Long studyGroupId) {
        // TODO: QueryRepository 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
