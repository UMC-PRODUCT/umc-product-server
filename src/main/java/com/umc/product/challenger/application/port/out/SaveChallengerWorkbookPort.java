package com.umc.product.challenger.application.port.out;

import com.umc.product.curriculum.domain.ChallengerWorkbook;

public interface SaveChallengerWorkbookPort {
    ChallengerWorkbook save(ChallengerWorkbook challengerWorkbook);
}
