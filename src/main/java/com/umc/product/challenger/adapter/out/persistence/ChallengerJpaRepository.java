package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.domain.Challenger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerJpaRepository extends JpaRepository<Challenger, Long> {

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

    Long countByIdIn(Set<Long> challengerIds);

    Optional<Challenger> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Challenger> findByIdIn(Set<Long> ids);
}
