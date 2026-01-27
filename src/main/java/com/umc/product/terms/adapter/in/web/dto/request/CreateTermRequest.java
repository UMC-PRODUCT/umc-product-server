package com.umc.product.terms.adapter.in.web.dto.request;

import com.umc.product.terms.domain.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTermRequest(
        @Schema(description = "약관 제목", example = "서비스 이용약관")
        @NotBlank(message = "약관 제목은 필수입니다.")
        String title,

        @Schema(description = "약관 상세 내용", example = "제 1조... 본 약관은 서비스 이용에 관한 사항을 규정합니다.")
        @NotBlank(message = "약관 내용은 필수입니다.")
        String content,

        @Schema(description = "약관 버전", example = "v1.2")
        @NotBlank(message = "약관 버전은 필수입니다.")
        String version,

        @Schema(description = "필수 동의 여부", example = "true")
        @NotNull(message = "필수 동의 약관 여부는 필수입니다.")
        boolean isMandatory,

        @Schema(description = "약관 카테고리 타입", example = "SERVICE")
        @NotNull(message = "약관 타입은 필수입니다.")
        TermsType termsType,

        @Schema(description = "약관 시행일 (ISO 8601 형식)", example = "2026-01-27T14:00:00Z")
        @NotNull(message = "시행일은 필수입니다.")
        Instant effectiveDate
) {
}
