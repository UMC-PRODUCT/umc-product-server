package com.umc.product.curriculum.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "배포된 주차 번호 목록")
public record AvailableWeeksResponse(
        @Schema(description = "배포된 주차 번호 목록 (오름차순)", example = "[1, 2, 3, 4, 5]")
        List<Integer> weeks
) {

    public static AvailableWeeksResponse from(List<Integer> weeks) {
        return new AvailableWeeksResponse(weeks);
    }
}
