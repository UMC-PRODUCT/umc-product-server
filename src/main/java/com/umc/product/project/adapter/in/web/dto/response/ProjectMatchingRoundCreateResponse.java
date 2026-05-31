package com.umc.product.project.adapter.in.web.dto.response;

public record ProjectMatchingRoundCreateResponse(
    Long matchingRoundId
) {
    public static ProjectMatchingRoundCreateResponse from(Long matchingRoundId) {
        return new ProjectMatchingRoundCreateResponse(matchingRoundId);
    }
}
