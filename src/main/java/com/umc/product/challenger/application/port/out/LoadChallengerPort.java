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
    List<Challenger> getAllByMemberId(Long memberId);

    /**
     * gisuId로 챌린저 목록 조회
     */
    List<Challenger> getAllByGisuId(Long gisuId);

    /**
     * 여러 gisuId로 챌린저 목록 조회
     */
    List<Challenger> getAllByGisuIds(List<Long> gisuIds);

    @Deprecated(since = "v1.5.0", forRemoval = true)
    Long countByIdIn(Set<Long> ids);

    /**
     * memberId로 가장 최근 챌린저 조회
     */
    Challenger findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 여러 ID로 챌린저 배치 조회
     */
    List<Challenger> getAllByIds(Set<Long> ids);

    /**
     * 특정 기수 내 여러 memberId 에 해당하는 챌린저를 batch 조회한다.
     * <p>
     * 입력된 모든 memberId 가 해당 기수의 챌린저로 존재해야 한다 -- 누락 시 도메인 invariant 위반으로 간주하고 예외를 던진다 ({@code batchGet} 시맨틱).
     */
    List<Challenger> batchGetByMemberIdsAndGisuId(Set<Long> memberIds, Long gisuId);

    /**
     * 특정 기수 내 여러 memberId 에 해당하는 챌린저를 조회한다.
     * <p>
     * 챌린저가 없는 memberId 는 결과에 포함하지 않는다.
     */
    List<Challenger> listByMemberIdsAndGisuId(Set<Long> memberIds, Long gisuId);

    /**
     * 각 멤버별 가장 최근 기수(gisuId 최대값)의 챌린저 목록 조회
     */
    List<Challenger> findLatestPerMember();
}
