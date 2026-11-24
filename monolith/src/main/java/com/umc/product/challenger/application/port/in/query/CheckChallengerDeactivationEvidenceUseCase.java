package com.umc.product.challenger.application.port.in.query;

public interface CheckChallengerDeactivationEvidenceUseCase {

    boolean isImageInUse(String imageId);

    boolean isImageInUseOutsideDeactivation(String imageId, Long deactivationId);
}
