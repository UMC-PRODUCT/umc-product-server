package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.domain.ChallengerMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerMissionJpaRepository extends JpaRepository<ChallengerMission, Long> {

    List<ChallengerMission> findByChallengerWorkbookId(Long challengerWorkbookId);
}
