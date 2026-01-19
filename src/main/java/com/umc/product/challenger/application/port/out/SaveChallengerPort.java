package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.Challenger;

public interface SaveChallengerPort {

    Challenger save(Challenger challenger);
}
