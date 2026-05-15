package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * 프로젝트 시딩 결과. ADR-017 참조.
 */
public record SeedProjectsResult(
    List<Long> createdProjectIds,
    List<SkippedCell> skippedChapters,
    int failedCount
) {

    /**
     * 풀 부족 또는 셀 단위 실패로 스킵된 (Chapter, School) 셀 정보.
     */
    public record SkippedCell(
        Long chapterId,
        Long schoolId,
        String reason
    ) {
    }
}
