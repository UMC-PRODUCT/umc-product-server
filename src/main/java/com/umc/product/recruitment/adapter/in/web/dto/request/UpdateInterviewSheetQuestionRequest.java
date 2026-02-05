package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateInterviewSheetQuestionRequest(
        @NotBlank String questionText
) {
}
