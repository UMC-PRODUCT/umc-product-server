package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    Optional<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookId(Long memberId, Long originalWorkbookId);

}
