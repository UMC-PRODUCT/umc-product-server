package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.util.List;

public record InterviewSchedulingApplicantsInfo(
    List<AvailableApplicantInfo> available,
    List<AlreadyScheduledApplicantInfo> alreadyScheduled
) {
    public record AvailableApplicantInfo(
        Long applicationId,
        String nickname,
        String name,
        PartOption firstPart,
        PartOption secondPart,
        double documentScore
    ) {
    }

    public record AlreadyScheduledApplicantInfo(
        Long applicationId,
        Long assignmentId,
        String nickname,
        String name,
        PartOption firstPart,
        PartOption secondPart,
        double documentScore,
        ScheduledSlotInfo scheduledSlot
    ) {
        public record ScheduledSlotInfo(String date, String start, String end) {
        }
    }
}
