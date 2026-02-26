package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GetWorkbookSubmissionsQuery(
        Long schoolId,
        @NotNull(message = "주차는 필수입니다")
        Integer weekNo,
        Long studyGroupId,
        ChallengerPart part,
        Long cursor,
        @Min(1) @Max(20)
        int size
) {
    private static final int DEFAULT_SIZE = 20;

    public GetWorkbookSubmissionsQuery {
        if (size == 0) {
            size = DEFAULT_SIZE;
        }
    }

    public int fetchSize() {
        return size + 1;
    }
}
