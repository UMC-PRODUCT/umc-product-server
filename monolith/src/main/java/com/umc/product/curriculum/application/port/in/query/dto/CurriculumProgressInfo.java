package com.umc.product.curriculum.application.port.in.query.dto;

import java.util.List;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record CurriculumProgressInfo(
    Long curriculumId,
    String curriculumTitle,
    String part,
    int completedCount,
    int totalCount,
    List<WorkbookProgressInfo> workbooks
) {

    public record WorkbookProgressInfo(
        Long originalWorkbookId, // TODO: OriginalWorkbookId은 나중에 삭제해야합니다.
        Long challengerWorkbookId,
        Integer weekNo,
        String title,
        String description,
        MissionType missionType,
        WorkbookStatus status,
        boolean isReleased,
        boolean isInProgress
    ) {
    }
}
