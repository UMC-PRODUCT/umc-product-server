package com.umc.product.curriculum.application.port.in.command.dto;

import com.umc.product.curriculum.domain.enums.MissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 미션 생성 커맨드
 *
 * @param originalWorkbookId 소속 원본 워크북 ID
 * @param title              미션 제목
 * @param description        미션 설명 (nullable)
 * @param missionType        미션 제출 유형 (LINK / MEMO / PLAIN)
 * @param isNecessary        미션 필수 수행 여부
 */
@Builder
public record CreateOriginalWorkbookMissionCommand(
    @NotNull(message = "원본 워크북 ID는 필수입니다")
    Long originalWorkbookId,

    @NotBlank(message = "미션 제목은 필수입니다")
    String title,

    String description,

    @NotNull(message = "미션 유형은 필수입니다")
    MissionType missionType,

    @NotNull(message = "필수 여부는 필수입니다")
    Boolean isNecessary
) {
}
