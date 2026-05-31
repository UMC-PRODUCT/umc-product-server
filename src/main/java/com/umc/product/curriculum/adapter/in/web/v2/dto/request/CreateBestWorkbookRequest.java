package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

public record CreateBestWorkbookRequest(
    Long bestMemberId,
    Long weeklyCurriculumId,
    Long studyGroupId, // NOT NULL!
    String reason
) {
}
