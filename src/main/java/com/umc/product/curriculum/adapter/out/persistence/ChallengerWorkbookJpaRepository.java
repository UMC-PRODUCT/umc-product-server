package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    Optional<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookId(Long memberId, Long originalWorkbookId);

    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);

}
