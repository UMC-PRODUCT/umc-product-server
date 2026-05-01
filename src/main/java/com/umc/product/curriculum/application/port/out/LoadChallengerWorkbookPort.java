package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;

public interface LoadChallengerWorkbookPort {

    ChallengerWorkbook findById(Long id);

    /**
     * memberId + 여러 originalWorkbookId로 ChallengerWorkbook 일괄 조회
     */
    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);
}
