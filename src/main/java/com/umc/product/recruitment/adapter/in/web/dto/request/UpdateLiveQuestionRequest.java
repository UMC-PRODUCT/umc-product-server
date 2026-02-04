package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLiveQuestionRequest(
        @NotBlank
        @Size(max = 238)
        String text
) {
}
