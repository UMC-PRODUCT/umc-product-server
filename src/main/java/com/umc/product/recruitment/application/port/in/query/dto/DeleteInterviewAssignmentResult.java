package com.umc.product.recruitment.application.port.in.query.dto;

public record DeleteInterviewAssignmentResult(
        UnassignedInfo unassigned,
        InterviewSchedulingSummaryInfo summary
) {
    public record UnassignedInfo(Long applicationId) {
    }
}