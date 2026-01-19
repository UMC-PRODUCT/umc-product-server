package com.umc.product.challenger.application.port.out;

import com.umc.product.curriculum.domain.ChallengerMission;
import java.util.List;
import java.util.Optional;

public interface LoadChallengerMissionPort {

    Optional<ChallengerMission> findById(Long id);

    List<ChallengerMission> findByChallengerWorkbookId(Long challengerWorkbookId);
}
