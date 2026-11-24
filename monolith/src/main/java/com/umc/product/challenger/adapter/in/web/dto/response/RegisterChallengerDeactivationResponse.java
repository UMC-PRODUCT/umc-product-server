package com.umc.product.challenger.adapter.in.web.dto.response;

public record RegisterChallengerDeactivationResponse(Long deactivationId) {

    public static RegisterChallengerDeactivationResponse from(Long deactivationId) {
        return new RegisterChallengerDeactivationResponse(deactivationId);
    }
}
