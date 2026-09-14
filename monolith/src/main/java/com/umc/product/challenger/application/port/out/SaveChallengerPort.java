package com.umc.product.challenger.application.port.out;

import java.util.List;

import com.umc.product.challenger.domain.Challenger;

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
