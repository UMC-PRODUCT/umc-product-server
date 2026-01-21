package com.umc.product.support;

import com.umc.product.challenger.domain.Challenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestChallengerRepository extends JpaRepository<Challenger, Long> {
}
