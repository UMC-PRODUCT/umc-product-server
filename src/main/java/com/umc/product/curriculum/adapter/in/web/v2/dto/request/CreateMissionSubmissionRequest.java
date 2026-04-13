package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

public record CreateMissionSubmissionRequest(
    Long originalWorkbookMissionId,
    Long challengerMissionId
) {
}
