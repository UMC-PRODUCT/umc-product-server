package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;
import java.util.Optional;

public interface LoadChallengerWorkbookPort {

    ChallengerWorkbook findById(Long id);

    /**
     * memberId + originalWorkbookId로 ChallengerWorkbook 단건 조회
     * <p>
     * (member_id, original_workbook_id) UNIQUE 제약이 있으므로 Optional 반환
     */
    Optional<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookId(Long memberId, Long originalWorkbookId);

    /**
     * memberId + 여러 originalWorkbookId로 ChallengerWorkbook 일괄 조회 (N+1 방지)
     */
    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);
}
