package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.Challenger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadChallengerPort {

    /**
     * ID로 챌린저 조회
     */
    Optional<Challenger> findById(Long id);

    /**
     * ID로 챌린저 조회 - 없으면 예외
     */
    Challenger getById(Long id);

    /**
     * memberId와 gisuId로 챌린저 조회
     */
    Optional<Challenger> findByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId로 챌린저 목록 조회
     */
    List<Challenger> findByMemberId(Long memberId);

    /**
     * gisuId로 챌린저 목록 조회
     */
    List<Challenger> findByGisuId(Long gisuId);

    Long countByIdIn(Set<Long> ids);

    /**
     * memberId로 가장 최근 챌린저 조회
     */
    Challenger findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 여러 ID로 챌린저 배치 조회
     */
    List<Challenger> findByIdIn(Set<Long> ids);
}
