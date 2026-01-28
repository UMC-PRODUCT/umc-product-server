package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.ChallengerWorkbook;
import java.util.List;

public interface LoadChallengerWorkbookPort {

    ChallengerWorkbook findById(Long id);

    List<Long> findOriginalWorkbookIdsWithSubmissions(List<Long> originalWorkbookIds);
}
