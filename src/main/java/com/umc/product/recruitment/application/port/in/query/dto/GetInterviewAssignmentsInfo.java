package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.domain.enums.EvaluationProgressStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record GetInterviewAssignmentsInfo(
        Instant serverNow,
        LocalDate selectedDate,
        PartOption selectedPart,
        List<InterviewAssignmentSlotInfo> interviewAssignmentSlots
) {
    public record InterviewAssignmentSlotInfo(
            Long assignmentId,
            SlotInfo slot,
            Long applicationId,
            ApplicantInfo applicant,
            List<AppliedPartInfo> appliedParts,
            Double documentScore,
            EvaluationProgressStatus evaluationProgressStatus
    ) {
    }

    public record SlotInfo(
            Long slotId,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {
    }

    public record ApplicantInfo(String nickname, String name) {
    }

    public record AppliedPartInfo(Integer priority, String key, String label) {
    }
}