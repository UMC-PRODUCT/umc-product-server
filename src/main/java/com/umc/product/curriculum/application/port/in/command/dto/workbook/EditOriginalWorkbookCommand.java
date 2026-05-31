package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 수정 커맨드
 *
 * @param originalWorkbookId 수정 대상 원본 워크북 ID
 * @param title              변경할 제목 (nullable: 미제공 시 유지)
 * @param description        변경할 설명 (nullable: 미제공 시 유지)
 * @param url                변경할 URL (nullable: 미제공 시 유지)
 * @param content            변경할 본문 내용 (nullable: 미제공 시 유지)
 */
@Builder
public record EditOriginalWorkbookCommand(
    @NotNull(message = "원본 워크북 ID는 필수입니다")
    Long originalWorkbookId,

    String title,
    String description,
    String url,
    String content
) {
}
