package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentResult;

public record CreateInterviewAssignmentResponse(
        Assigned assigned,
        InterviewSchedulingSummaryResponse summary
) {
    public static CreateInterviewAssignmentResponse from(CreateInterviewAssignmentResult result) {
        var a = result.assigned();
        return new CreateInterviewAssignmentResponse(
                new Assigned(
                        a.assignmentId(),
                        a.applicationId(),
                        new Slot(a.slot().slotId(), a.slot().date(), a.slot().start(), a.slot().end())
                ),
                InterviewSchedulingSummaryResponse.from(result.summary())
        );
    }

    public record Assigned(
            Long assignmentId,
            Long applicationId,
            Slot slot
    ) {
    }

    public record Slot(
            Long slotId,
            String date,
            String start,
            String end
    ) {
    }
}
