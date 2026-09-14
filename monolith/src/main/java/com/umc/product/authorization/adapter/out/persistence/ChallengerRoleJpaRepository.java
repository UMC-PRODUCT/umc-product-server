package com.umc.product.authorization.adapter.out.persistence;

import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerRoleJpaRepository extends JpaRepository<ChallengerRole, Long> {

    /**
     * Challenger ID로 Role 조회
     */
    List<ChallengerRole> findByChallengerId(Long challengerId);

    /**
     * 여러 Challenger ID로 Role 일괄 조회
     */
    List<ChallengerRole> findByChallengerIdIn(Set<Long> challengerIds);

    /**
     * 기수 ID로 Role 조회
     */
    List<ChallengerRole> findByGisuId(Long gisuId);
}
