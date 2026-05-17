package com.umc.product.authentication.adapter.in.web.dto.response;

public record EmailAvailabilityResponse(
    String email,
    boolean available
) {
    public static EmailAvailabilityResponse of(String email, boolean available) {
        return new EmailAvailabilityResponse(email, available);
    }
}
