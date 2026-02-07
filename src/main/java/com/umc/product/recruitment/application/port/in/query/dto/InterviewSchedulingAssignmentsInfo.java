package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.util.List;

public record InterviewSchedulingAssignmentsInfo(
    List<InterviewAssignmentInfo> assignments
) {
    public record InterviewAssignmentInfo(
        Long assignmentId,
        Long applicationId,
        String nickname,
        String name,
        PartOption firstPart,
        PartOption secondPart,
        double documentScore
    ) {
    }
}
