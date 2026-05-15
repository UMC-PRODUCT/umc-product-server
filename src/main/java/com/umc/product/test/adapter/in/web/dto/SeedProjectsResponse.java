package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult;
import java.util.List;

public record SeedProjectsResponse(
    List<Long> createdProjectIds,
    List<SkippedCell> skippedChapters,
    int failedCount
) {

    public record SkippedCell(
        Long chapterId,
        Long schoolId,
        String reason
    ) {

        public static SkippedCell from(SeedProjectsResult.SkippedCell src) {
            return new SkippedCell(src.chapterId(), src.schoolId(), src.reason());
        }
    }

    public static SeedProjectsResponse from(SeedProjectsResult result) {
        return new SeedProjectsResponse(
            result.createdProjectIds(),
            result.skippedChapters().stream().map(SkippedCell::from).toList(),
            result.failedCount()
        );
    }
}
