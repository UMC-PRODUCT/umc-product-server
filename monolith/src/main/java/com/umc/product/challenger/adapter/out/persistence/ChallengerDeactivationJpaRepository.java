package com.umc.product.challenger.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.umc.product.challenger.domain.ChallengerDeactivation;

import jakarta.persistence.LockModeType;

public interface ChallengerDeactivationJpaRepository extends JpaRepository<ChallengerDeactivation, Long> {

    boolean existsByChallengerId(Long challengerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChallengerDeactivation> findByChallengerId(Long challengerId);

    List<ChallengerDeactivation> findByCreatedAtLessThanEqualAndIdGreaterThanOrderByIdAsc(
        Instant createdAt,
        Long id,
        Pageable pageable
    );
}
