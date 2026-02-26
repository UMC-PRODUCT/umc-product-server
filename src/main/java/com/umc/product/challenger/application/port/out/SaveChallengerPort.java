package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.Challenger;
import java.util.List;

public interface SaveChallengerPort {

    /**
     * 챌린저 저장
     */
    Challenger save(Challenger challenger);

    List<Challenger> saveAll(List<Challenger> challengers);

    /**
     * 챌린저 삭제
     */
    void delete(Challenger challenger);
}
