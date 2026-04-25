package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

//    List<ChallengerWorkbook> findAllByChallengerIdAndOriginalWorkbookIdOrderByIdDesc(
//        Long challengerId,
//        Long originalWorkbookId
//    );
//
//    // OriginalWorkbook을 통해 챌린저에게 배포된 WorkBook이 있는지 find
//    @Query("SELECT DISTINCT cw.originalWorkbookId FROM ChallengerWorkbook cw WHERE cw.originalWorkbookId IN :ids")
//    List<Long> findOriginalWorkbookIdsByOriginalWorkbookIdIn(@Param("ids") List<Long> ids);
}
