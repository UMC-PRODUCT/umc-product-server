package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.domain.ChallengerPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerPointJpaRepository extends JpaRepository<ChallengerPoint, Long> {
}
