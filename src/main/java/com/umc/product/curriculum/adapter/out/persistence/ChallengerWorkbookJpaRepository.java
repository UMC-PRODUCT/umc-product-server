package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);

}
