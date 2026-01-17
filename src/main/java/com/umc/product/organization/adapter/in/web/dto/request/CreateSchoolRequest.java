package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "학교 생성 요청")
public record CreateSchoolRequest(
        @Schema(description = "학교명", example = "서울대학교", minLength = 2, maxLength = 50)
        @NotBlank @Size(min = 2, max = 50, message = "학교명은 2~50자")
        String schoolName,

        @Schema(description = "지부 ID", example = "1")
        @Positive(message = "chapterId는 양수여야 합니다")
        Long chapterId,

        @Schema(description = "비고", example = "관악캠퍼스", maxLength = 200)
        @NotNull @Size(max = 200, message = "비고는 200자 이내")
        String remark
) {
    public CreateSchoolCommand toCommand() {
        return new CreateSchoolCommand(schoolName, chapterId, remark);
    }
}
