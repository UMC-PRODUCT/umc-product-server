package com.umc.product.term.adapter.in.web.dto.request;

import com.umc.product.term.domain.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTermRequest(
    @Schema(description = "약관 링크",
        example = "https://makeus-challenge.notion.site/300b57f4596b803f8c94dd4f4fb71960?source=copy_link")
    @NotBlank(message = "약관 링크는 필수입니다.")
    String link,

    @Schema(description = "필수 동의 여부", example = "true")
    @NotNull(message = "필수 동의 약관 여부는 필수입니다.")
    boolean isMandatory,

    @Schema(description = "약관 카테고리 타입", example = "PRIVACY")
    @NotNull(message = "약관 타입은 필수입니다.")
    TermType termType
) {
}
