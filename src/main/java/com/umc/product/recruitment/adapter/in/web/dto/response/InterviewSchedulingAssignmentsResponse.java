package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingAssignmentsInfo;
import java.util.List;

public record InterviewSchedulingAssignmentsResponse(
    List<InterviewAssignmentResponse> assignments
) {
    public static InterviewSchedulingAssignmentsResponse from(
        InterviewSchedulingAssignmentsInfo info
    ) {
        return new InterviewSchedulingAssignmentsResponse(
            info.assignments().stream()
                .map(i -> new InterviewAssignmentResponse(
                    i.assignmentId(),
                    i.applicationId(),
                    i.nickname(),
                    i.name(),
                    toPreferredPart(i.firstPart()),
                    toPreferredPart(i.secondPart()),
                    i.documentScore()
                ))
                .toList()
        );
    }

    private static PreferredPartResponse toPreferredPart(PartOption part) {
        if (part == null) {
            return null;
        }
        return new PreferredPartResponse(part.name(), part.getLabel());
    }

    public record InterviewAssignmentResponse(
        Long assignmentId,
        Long applicationId,
        String nickname,
        String name,
        PreferredPartResponse firstPart,
        PreferredPartResponse secondPart,
        double documentScore
    ) {
    }

    public record PreferredPartResponse(String part, String label) {
    }
}
