package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerPoint;

public interface SaveChallengerPointPort {

    /**
     * 챌린저 포인트 저장
     */
    ChallengerPoint save(ChallengerPoint challengerPoint);

    /**
     * 챌린저 포인트 삭제
     */
    void delete(ChallengerPoint challengerPoint);
}
