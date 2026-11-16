package com.umc.product.challenger.application.port.out.dto;

import java.util.List;

public record ChallengerDeactivationRetentionTarget(
    Long deactivationId,
    List<String> imageIds
) {
}
