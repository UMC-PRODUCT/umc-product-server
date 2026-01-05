package com.umc.product.curriculum.application.port.in.dto;

import com.umc.product.curriculum.domain.MissionType;
import com.umc.product.curriculum.domain.WorkbookStatus;
import java.util.List;

public record CurriculumDetailResponse(
        Long curriculumId,
        Long gisuId,
        String part,
        List<WorkbookProgressInfo> workbooks
) {

    public record WorkbookProgressInfo(
            Long workbookId,
            Integer weekNo,
            String title,
            String workbookUrl,
            WorkbookStatus status,
            boolean isLocked,
            MissionInfo mission
    ) {
    }

    public record MissionInfo(
            Long missionId,
            String title,
            MissionType missionType,  // LINK, MEMO, PLAIN 중 하나
            SubmissionInfo submission
    ) {
    }

    public record SubmissionInfo(
            boolean isSubmitted,
            String content  // LINK면 URL, MEMO면 텍스트, PLAIN이면 null
    ) {
    }
}
