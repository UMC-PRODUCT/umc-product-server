package com.umc.product.challenger.application.port.out;

import java.time.Instant;
import java.util.List;

import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;
import com.umc.product.challenger.domain.ChallengerDeactivation;

public interface LoadChallengerDeactivationPort {

    boolean existsByChallengerId(Long challengerId);

    ChallengerDeactivation getByChallengerIdForUpdate(Long challengerId);

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
