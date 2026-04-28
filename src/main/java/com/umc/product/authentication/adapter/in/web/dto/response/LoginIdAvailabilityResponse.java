package com.umc.product.authentication.adapter.in.web.dto.response;

public record LoginIdAvailabilityResponse(
    String loginId,
    boolean available
) {
    public static LoginIdAvailabilityResponse of(String loginId, boolean available) {
        return new LoginIdAvailabilityResponse(loginId, available);
    }
}
