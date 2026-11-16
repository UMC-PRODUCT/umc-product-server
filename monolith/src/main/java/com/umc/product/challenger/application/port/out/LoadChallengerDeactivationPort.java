package com.umc.product.challenger.application.port.out;

import java.time.Instant;
import java.util.List;

import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;

public interface LoadChallengerDeactivationPort {

    boolean existsByChallengerId(Long challengerId);

    boolean existsByImageId(String imageId);

    boolean existsImageReferenceOutsideDeactivation(String imageId, Long deactivationId);

    List<ChallengerDeactivationRetentionTarget> listRetentionTargets(
        Instant createdAtThreshold,
        Long afterId,
        int limit
    );
}
