package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamSquad;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTeamSquadJpaRepository extends JpaRepository<ProductTeamSquad, Long> {

    List<ProductTeamSquad> findAllByOrderBySortOrderAscIdAsc();

    List<ProductTeamSquad> findAllByIsActiveOrderBySortOrderAscIdAsc(boolean isActive);

    List<ProductTeamSquad> findByIdIn(Collection<Long> ids);

    @Query("""
        SELECT s
        FROM ProductTeamSquad s
        WHERE s.startAt IS NOT NULL
          AND s.endAt IS NOT NULL
          AND s.startAt < :endAt
          AND s.endAt > :startAt
        ORDER BY s.sortOrder ASC, s.id ASC
        """)
    List<ProductTeamSquad> findAllOverlapping(@Param("startAt") Instant startAt, @Param("endAt") Instant endAt);
}
