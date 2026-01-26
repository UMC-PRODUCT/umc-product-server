package com.umc.product.curriculum.application.port.out;

import java.util.List;

public interface LoadChallengerWorkbookPort {

    List<Long> findOriginalWorkbookIdsWithSubmissions(List<Long> originalWorkbookIds);
}
