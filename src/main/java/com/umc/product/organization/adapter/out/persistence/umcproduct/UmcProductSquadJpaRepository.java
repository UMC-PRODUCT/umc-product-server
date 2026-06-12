package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.organization.domain.UmcProductSquad;

public interface UmcProductSquadJpaRepository extends JpaRepository<UmcProductSquad, Long> {

    List<UmcProductSquad> findAllByOrderBySortOrderAscIdAsc();

    List<UmcProductSquad> findAllByIsActiveOrderBySortOrderAscIdAsc(boolean isActive);

    List<UmcProductSquad> findByIdIn(Collection<Long> ids);

    @Query("""
        SELECT s
        FROM UmcProductSquad s
        WHERE s.startAt IS NOT NULL
          AND s.endAt IS NOT NULL
          AND s.startAt < :endAt
          AND s.endAt > :startAt
        ORDER BY s.sortOrder ASC, s.id ASC
        """)
    List<UmcProductSquad> findAllOverlapping(@Param("startAt") Instant startAt, @Param("endAt") Instant endAt);
}
