package com.umc.product.challenger.adapter.out.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.challenger.domain.ChallengerDeactivation;

public interface ChallengerDeactivationJpaRepository extends JpaRepository<ChallengerDeactivation, Long> {

    boolean existsByChallengerId(Long challengerId);

    List<ChallengerDeactivation> findByCreatedAtLessThanEqualAndIdGreaterThanOrderByIdAsc(
        Instant createdAt,
        Long id,
        Pageable pageable
    );
}
