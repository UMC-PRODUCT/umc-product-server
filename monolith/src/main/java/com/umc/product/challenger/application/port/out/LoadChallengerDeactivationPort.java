package com.umc.product.challenger.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;

public interface LoadChallengerDeactivationPort {

    boolean existsByChallengerId(Long challengerId);

    ChallengerDeactivation getByChallengerIdForUpdate(Long challengerId);

    ChallengerDeactivation getByIdForUpdate(Long deactivationId);

    Optional<ChallengerDeactivation> findById(Long deactivationId);

    List<ChallengerDeactivationHistory> listHistoriesByDeactivationId(Long deactivationId);

    List<String> listOrderedImageIdsByDeactivationId(Long deactivationId);

    List<String> listOrderedLinksByDeactivationId(Long deactivationId);

    List<String> listImageIdsByDeactivationId(Long deactivationId);

    long countImagesByDeactivationId(Long deactivationId);

    long countLinksByDeactivationId(Long deactivationId);

    boolean existsAnyImageByDeactivationId(Long deactivationId, List<String> imageIds);

    boolean existsAnyLinkByDeactivationId(Long deactivationId, List<String> links);

    boolean existsByImageId(String imageId);

    boolean existsImageReferenceOutsideDeactivation(String imageId, Long deactivationId);

    List<ChallengerDeactivationRetentionTarget> listRetentionTargets(
        Instant createdAtThreshold,
        Long afterId,
        int limit
    );
}
