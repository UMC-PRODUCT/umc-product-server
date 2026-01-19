package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.domain.Challenger;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengerJpaRepository extends JpaRepository<Challenger, Long> {

    Optional<Challenger> findByMemberIdAndGisuId(Long memberId, Long gisuId);
}
