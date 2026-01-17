package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateSchoolRequest(
        @NotBlank @Size(min = 2, max = 50, message = "학교명은 2~50자") String schoolName,
        @Positive(message = "chapterId는 양수여야 합니다") Long chapterId,
        @NotNull @Size(max = 200, message = "비고는 200자 이내") String remark) {
    public CreateSchoolCommand toCommand() {
        return new CreateSchoolCommand(schoolName, chapterId, remark);
    }
}
