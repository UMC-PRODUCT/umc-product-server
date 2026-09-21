package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 선택 가능한 파트 응답. 클라이언트의 파트 선택 UI(공지 대상 선택 등)에서 사용하는 단일 소스입니다.
 */
@Schema(description = "선택 가능한 파트")
public record PartResponse(
    @Schema(description = "파트 코드", example = "WEB_PRODUCT_ENGINEER") String name,
    @Schema(description = "파트 표시명", example = "웹 프로덕트 엔지니어") String displayName
) {
    public static PartResponse from(ChallengerPart part) {
        return new PartResponse(part.name(), part.getDisplayName());
    }
}
