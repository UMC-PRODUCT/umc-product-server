package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.DeleteInterviewAssignmentResult;

public record DeleteInterviewAssignmentResponse(
        Unassigned unassigned,
        InterviewSchedulingSummaryResponse summary
) {
    public static DeleteInterviewAssignmentResponse from(DeleteInterviewAssignmentResult result) {
        return new DeleteInterviewAssignmentResponse(
                new Unassigned(result.unassigned().applicationId()),
                InterviewSchedulingSummaryResponse.from(result.summary())
        );
    }

    public record Unassigned(Long applicationId) {
    }
}