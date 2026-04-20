package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 커리큘럼 수정 커맨드
 *
 * @param curriculumId 수정 대상 커리큘럼 ID
 * @param title        변경할 커리큘럼 제목
 */
@Builder
public record EditCurriculumCommand(
    @NotNull(message = "커리큘럼 ID는 필수입니다")
    Long curriculumId,

    @NotBlank(message = "제목은 필수입니다")
    String title
) {
}