package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOriginalWorkbookRequest(
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
    public CreateOriginalWorkbookCommand toCommand(OriginalWorkbookStatus initialStatus) {
        return CreateOriginalWorkbookCommand.builder()
            .weeklyCurriculumId(weeklyCurriculumId)
            .title(title)
            .description(description)
            .url(url)
            .content(content)
            .type(type)
            .initialStatus(initialStatus)
            .build();
    }
}