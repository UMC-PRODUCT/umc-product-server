package com.umc.product.curriculum.adapter.out.persistence.repository;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);

}
