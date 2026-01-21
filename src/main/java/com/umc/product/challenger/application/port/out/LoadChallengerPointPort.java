package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerPoint;
import java.util.Optional;

public interface LoadChallengerPointPort {

    /**
     * ID로 챌린저 포인트 조회
     */
    Optional<ChallengerPoint> findById(Long id);

    /**
     * ID로 챌린저 포인트 조회 - 없으면 예외
     */
    ChallengerPoint getById(Long id);
}
