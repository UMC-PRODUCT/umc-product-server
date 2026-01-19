package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    Optional<ChallengerWorkbook> findByChallengerIdAndOriginalWorkbookId(Long challengerId, Long originalWorkbookId);

    List<ChallengerWorkbook> findByChallengerId(Long challengerId);

    List<ChallengerWorkbook> findByOriginalWorkbookId(Long originalWorkbookId);
}
