package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "지부 생성 요청")
public record CreateChapterRequest(
        @Schema(description = "기수 ID", example = "1")
        @NotNull(message = "기수 ID는 필수입니다")
        Long gisuId,

        @Schema(description = "지부명", example = "서울")
        @NotBlank(message = "지부명은 필수입니다")
        String name,

        @Schema(description = "소속 학교 ID 목록", example = "[1, 2, 3]")
        List<Long> schoolIds
) {
    public CreateChapterCommand toCommand() {
        return new CreateChapterCommand(
                gisuId,
                name,
                schoolIds != null ? schoolIds : List.of()
        );
    }
}
