package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import com.umc.product.curriculum.domain.enums.MissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 미션 수정 커맨드
 *
 * @param originalWorkbookMissionId 수정 대상 원본 워크북 미션 ID
 * @param title                     변경할 미션 제목 (nullable: 미제공 시 유지)
 * @param description               변경할 미션 설명 (nullable: 미제공 시 유지)
 * @param missionType               변경할 미션 유형 (nullable: 미제공 시 유지)
 * @param isNecessary               변경할 필수 수행 여부 (nullable: 미제공 시 유지, 배포 후 필수→선택 방향만 가능)
 */
@Builder
public record EditOriginalWorkbookMissionCommand(
    @NotNull(message = "원본 워크북 미션 ID는 필수입니다")
    Long originalWorkbookMissionId,

    String title,
    String description,
    MissionType missionType,
    Boolean isNecessary
) {
}
