package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerWorkbook;
import java.util.List;
import java.util.Optional;

public interface LoadChallengerWorkbookPort {

    Optional<ChallengerWorkbook> findById(Long id);

    Optional<ChallengerWorkbook> findByChallengerIdAndOriginalWorkbookId(Long challengerId, Long originalWorkbookId);

    List<ChallengerWorkbook> findByChallengerId(Long challengerId);

    List<ChallengerWorkbook> findByChallengerIdAndCurriculumId(Long challengerId, Long curriculumId);

    List<ChallengerWorkbook> findByOriginalWorkbookId(Long originalWorkbookId);

    List<ChallengerWorkbook> findByOriginalWorkbookIdAndStudyGroupId(Long originalWorkbookId, Long studyGroupId);
}
