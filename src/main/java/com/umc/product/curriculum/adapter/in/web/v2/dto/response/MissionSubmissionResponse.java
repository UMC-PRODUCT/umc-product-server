package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.MyCurriculumInfo.MissionSubmissionInfo;
import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record MissionSubmissionResponse(
    Long missionSubmissionId,
    Long originalWorkbookMissionId,
    MissionType submittedAsType,
    String submittedContent,
    Instant submittedAt,
    Instant lastEditedAt,
    MissionSubmissionStatusResponse status,
    boolean hasFeedback,
    List<MissionFeedbackResponse> feedbacks
) {

    static MissionSubmissionResponse from(Long originalWorkbookMissionId, MissionSubmissionInfo info) {
        return MissionSubmissionResponse.builder()
            .missionSubmissionId(info.missionSubmissionId())
            .originalWorkbookMissionId(originalWorkbookMissionId)
            .submittedAsType(info.submittedAsType())
            .submittedContent(info.submittedContent())
            .submittedAt(info.submittedAt())
            .lastEditedAt(info.lastEditedAt())
            .status(MissionSubmissionStatusResponse.from(info.status()))
            .hasFeedback(info.hasFeedback())
            .feedbacks(info.feedbacks().stream()
                .map(MissionFeedbackResponse::from)
                .toList())
            .build();
    }
}