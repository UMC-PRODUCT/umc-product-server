package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerMissionPort;
import com.umc.product.challenger.domain.ChallengerMission;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerMissionPersistenceAdapter implements LoadChallengerMissionPort {

    private final ChallengerMissionJpaRepository challengerMissionJpaRepository;

    @Override
    public Optional<ChallengerMission> findById(Long id) {
        return challengerMissionJpaRepository.findById(id);
    }

    @Override
    public List<ChallengerMission> findByChallengerWorkbookId(Long challengerWorkbookId) {
        return challengerMissionJpaRepository.findByChallengerWorkbookId(challengerWorkbookId);
    }
}
