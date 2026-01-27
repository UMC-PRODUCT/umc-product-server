package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    // OriginalWorkbook을 통해 챌린저에게 배포된 WorkBook이 있는지 find
    @Query("SELECT DISTINCT cw.originalWorkbookId FROM ChallengerWorkbook cw WHERE cw.originalWorkbookId IN :ids")
    List<Long> findOriginalWorkbookIdsByOriginalWorkbookIdIn(@Param("ids") List<Long> ids);
}
