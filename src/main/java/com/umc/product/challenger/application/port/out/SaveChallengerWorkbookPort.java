package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerWorkbook;

public interface SaveChallengerWorkbookPort {
    ChallengerWorkbook save(ChallengerWorkbook challengerWorkbook);
}
