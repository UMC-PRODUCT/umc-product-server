package com.umc.product.terms.adapter.in.web.dto.request;

import com.umc.product.terms.domain.enums.TermsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTermRequest(
        @NotBlank(message = "약관 제목은 필수입니다.")
        String title,

        @NotBlank(message = "약관 내용은 필수입니다.")
        String content,

        @NotBlank(message = "약관 버전은 필수입니다.")
        String version,

        @NotNull(message = "필수 동의 약관 여부는 필수입니다.")
        boolean isMandatory,

        @NotNull(message = "약관 타입은 필수입니다.")
        TermsType termsType,

        @NotNull(message = "시행일은 필수입니다.")
        Instant effectiveDate
) {
}
