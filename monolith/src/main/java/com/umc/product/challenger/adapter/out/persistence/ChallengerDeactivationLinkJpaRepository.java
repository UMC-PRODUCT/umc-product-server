package com.umc.product.challenger.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.challenger.domain.ChallengerDeactivationLink;

public interface ChallengerDeactivationLinkJpaRepository extends JpaRepository<ChallengerDeactivationLink, Long> {

    long countByAddedHistoryDeactivationId(Long deactivationId);

    boolean existsByLinkInAndAddedHistoryDeactivationId(List<String> links, Long deactivationId);

    @Modifying
    @Query("""
        delete from ChallengerDeactivationLink link
        where link.addedHistory.id in (
            select history.id
            from ChallengerDeactivationHistory history
            where history.deactivation.id = :deactivationId
        )
        """)
    int deleteByDeactivationId(@Param("deactivationId") Long deactivationId);
}
