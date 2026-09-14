package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerPoint;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadChallengerPointPort {

    List<ChallengerPoint> findByChallengerId(Long challengerId);

    /**
     * 여러 챌린저 ID로 포인트 일괄 조회
     */
    List<ChallengerPoint> findByChallengerIdIn(Set<Long> challengerIds);

    /**
     * ID로 챌린저 포인트 조회
     */
    Optional<ChallengerPoint> findById(Long id);

    /**
     * ID로 챌린저 포인트 조회 - 없으면 예외
     */
    ChallengerPoint getById(Long id);
}
