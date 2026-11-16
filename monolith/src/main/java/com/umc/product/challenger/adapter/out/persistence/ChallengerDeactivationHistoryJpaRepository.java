package com.umc.product.challenger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.challenger.domain.ChallengerDeactivationHistory;

public interface ChallengerDeactivationHistoryJpaRepository extends JpaRepository<ChallengerDeactivationHistory, Long> {
}
