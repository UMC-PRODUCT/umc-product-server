package com.umc.product.support;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.challenger.domain.Challenger;

public interface TestChallengerRepository extends JpaRepository<Challenger, Long> {
}
