package com.umc.product.curriculum.application.port.in.command.dto;

import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 생성 커맨드
 *
 * @param weeklyCurriculumId 소속 주차별 커리큘럼 ID
 * @param title              원본 워크북 제목
 * @param description        원본 워크북 설명 (nullable)
 * @param url                원본 워크북 URL (nullable)
 * @param content            원본 워크북 본문 내용 (nullable)
 * @param type               워크북 유형 (MAIN / EXTRA)
 */
@Builder
public record CreateOriginalWorkbookCommand(
    @NotNull(message = "주차별 커리큘럼 ID는 필수입니다")
    Long weeklyCurriculumId,

    @NotBlank(message = "제목은 필수입니다")
    String title,

    String description,
    String url,
    String content,

    @NotNull(message = "워크북 유형은 필수입니다")
    OriginalWorkbookType type
) {
}