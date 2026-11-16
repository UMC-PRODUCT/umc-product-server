package com.umc.product.challenger.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.challenger.domain.ChallengerDeactivationImage;

public interface ChallengerDeactivationImageJpaRepository extends JpaRepository<ChallengerDeactivationImage, Long> {

    boolean existsByImageId(String imageId);

    boolean existsByImageIdAndAddedHistoryDeactivationIdNot(String imageId, Long deactivationId);

    long countByAddedHistoryDeactivationId(Long deactivationId);

    boolean existsByImageIdInAndAddedHistoryDeactivationId(List<String> imageIds, Long deactivationId);

    @Query("""
        select image
        from ChallengerDeactivationImage image
        join fetch image.addedHistory history
        join fetch history.deactivation deactivation
        where deactivation.id in :deactivationIds
        """)
    List<ChallengerDeactivationImage> findAllWithDeactivationByDeactivationIdIn(
        @Param("deactivationIds") List<Long> deactivationIds
    );

    @Modifying
    @Query("""
        delete from ChallengerDeactivationImage image
        where image.addedHistory.id in (
            select history.id
            from ChallengerDeactivationHistory history
            where history.deactivation.id = :deactivationId
        )
        """)
    int deleteByDeactivationId(@Param("deactivationId") Long deactivationId);
}
