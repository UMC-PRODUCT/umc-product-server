package com.umc.product.member.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 동의 상태")
public record TermConsentStatus(

        @Schema(description = "약관 ID", example = "1")
        @NotNull(message = "약관 ID는 필수입니다")
        Long termsId,

        @Schema(description = "동의 여부", example = "true")
        boolean isAgreed
) {
}
