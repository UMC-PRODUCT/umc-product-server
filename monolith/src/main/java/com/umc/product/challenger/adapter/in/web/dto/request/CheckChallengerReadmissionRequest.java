package com.umc.product.challenger.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckChallengerReadmissionRequest(@NotBlank @Email @Size(max = 254) String email) {

    public CheckChallengerReadmissionRequest {
        email = email == null ? null : email.trim();
    }

    @Override
    public String toString() {
        return "CheckChallengerReadmissionRequest[email=**]";
    }
}
