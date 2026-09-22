package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.domain.enums.PointType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrantChallengerPointRequest(
    @Schema(description = "10기부터 우수 워크북은 BEST_WORKBOOK_V2를 사용합니다.")
    @NotNull(message = "상벌점 타입은 필수입니다") PointType pointType,
    @Schema(description = "CUSTOM은 필수입니다. 고정 유형은 생략하거나 해당 유형의 기본 배점과 같은 값만 전달할 수 있습니다.")
    Integer pointValue,
    @Size(max = 200, message = "상벌점 설명은 200자 이하여야 합니다") String description
) {
    public GrantChallengerPointCommand toCommand(Long challengerId) {
        return new GrantChallengerPointCommand(challengerId, pointType, pointValue, description);
    }
}
