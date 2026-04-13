package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.Instant;
import java.util.List;

public record MissionSubmissionResponse(
    Long missionSubmissionId,
    Long originalWorkbookMissionId,
    MissionType submittedAsType,
    String submittedContent,
    Instant submittedAt,
    Instant lastEditedAt,
    List<MissionFeedbackResponse> feedbacks
) {
}
