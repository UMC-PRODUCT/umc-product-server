package com.umc.product.challenger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.challenger.domain.ChallengerDeactivationHistory;

public interface ChallengerDeactivationHistoryJpaRepository extends JpaRepository<ChallengerDeactivationHistory, Long> {

    @Modifying
    @Query("delete from ChallengerDeactivationHistory history where history.deactivation.id = :deactivationId")
    int deleteByDeactivationId(@Param("deactivationId") Long deactivationId);
}
