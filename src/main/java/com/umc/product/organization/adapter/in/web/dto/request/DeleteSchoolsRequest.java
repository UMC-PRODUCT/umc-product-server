package com.umc.product.organization.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "학교 삭제 요청")
public record DeleteSchoolsRequest(
        @Schema(description = "삭제할 학교 ID 목록", example = "[1, 2, 3]")
        @NotEmpty
        List<Long> schoolIds
) {
}
