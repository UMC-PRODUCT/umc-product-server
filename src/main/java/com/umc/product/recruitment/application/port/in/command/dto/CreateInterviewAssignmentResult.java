package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSummaryInfo;

public record CreateInterviewAssignmentResult(
        AssignedInfo assigned,
        InterviewSchedulingSummaryInfo summary
) {
    public record AssignedInfo(
            Long assignmentId,
            Long applicationId,
            SlotInfo slot
    ) {
        public record SlotInfo(Long slotId, String date, String start, String end) {
        }
    }
}