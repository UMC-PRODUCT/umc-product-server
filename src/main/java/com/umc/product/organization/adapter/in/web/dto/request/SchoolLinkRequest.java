package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.SchoolLinkCommand;
import com.umc.product.organization.domain.SchoolLinkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학교 링크 요청")
public record SchoolLinkRequest(
        @Schema(description = "링크 제목", example = "UMC 카카오톡 오픈채팅")
        @NotBlank(message = "링크 제목은 필수입니다")
        String title,

        @Schema(description = "링크 타입", example = "KAKAO")
        @NotNull(message = "링크 타입은 필수입니다")
        SchoolLinkType type,

        @Schema(description = "링크 URL", example = "https://open.kakao.com/o/example")
        @NotBlank(message = "링크 URL은 필수입니다")
        String url
) {
    public SchoolLinkCommand toCommand() {
        return new SchoolLinkCommand(title, type, url);
    }
}
