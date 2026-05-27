package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult;
import java.util.List;

public record SeedProjectsResponse(
    List<Long> createdProjectIds,
    List<PartialProject> partialProjects,
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

    public record PartialProject(
        Long projectId,
        Long chapterId,
        Long schoolId,
        int addedMemberCount,
        int expectedMemberCount,
        String reason
    ) {

        public static PartialProject from(SeedProjectsResult.PartialProject src) {
            return new PartialProject(
                src.projectId(),
                src.chapterId(),
                src.schoolId(),
                src.addedMemberCount(),
                src.expectedMemberCount(),
                src.reason()
            );
        }
    }

    public static SeedProjectsResponse from(SeedProjectsResult result) {
        return new SeedProjectsResponse(
            result.createdProjectIds(),
            result.partialProjects().stream().map(PartialProject::from).toList(),
            result.skippedChapters().stream().map(SkippedCell::from).toList(),
            result.failedCount()
        );
    }
}
