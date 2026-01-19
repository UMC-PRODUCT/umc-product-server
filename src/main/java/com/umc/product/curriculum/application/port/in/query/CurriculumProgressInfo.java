package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import java.util.List;

public record CurriculumProgressInfo(
        Long curriculumId,
        String curriculumTitle,
        String part,
        int completedCount,
        int totalCount,
        List<WorkbookProgressInfo> workbooks
) {

    public record WorkbookProgressInfo(
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
