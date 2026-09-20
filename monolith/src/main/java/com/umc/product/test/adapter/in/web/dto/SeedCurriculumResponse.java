package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.test.application.port.in.command.dto.SeedCurriculumResult;

public record SeedCurriculumResponse(
    Long gisuId,
    List<Long> createdCurriculumIds,
    List<Long> createdWeeklyCurriculumIds,
    List<Long> createdOriginalWorkbookIds,
    List<Long> createdMissionIds,
    boolean released,
    int curriculumFailed,
    int weeklyCurriculumFailed,
    int originalWorkbookFailed,
    int missionFailed,
    int releaseFailed
) {

    public static SeedCurriculumResponse from(SeedCurriculumResult result) {
        return new SeedCurriculumResponse(
            result.gisuId(),
            result.createdCurriculumIds(),
            result.createdWeeklyCurriculumIds(),
            result.createdOriginalWorkbookIds(),
            result.createdMissionIds(),
            result.released(),
            result.curriculumFailed(),
            result.weeklyCurriculumFailed(),
            result.originalWorkbookFailed(),
            result.missionFailed(),
            result.releaseFailed()
        );
    }
}
