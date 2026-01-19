package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.challenger.domain.ChallengerWorkbook;
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
    public List<ChallengerWorkbook> findByOriginalWorkbookId(Long originalWorkbookId) {
        return challengerWorkbookJpaRepository.findByOriginalWorkbookId(originalWorkbookId);
    }

}
