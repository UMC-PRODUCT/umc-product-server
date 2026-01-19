package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.ApplicationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record MyApplicationListInfo(
        String nickName,
        String name,
        CurrentApplicationStatusInfo current,
        List<MyApplicationCardInfo> applications
) {
    public record CurrentApplicationStatusInfo(
            List<String> appliedParts,
            EvaluationStatusInfo documentEvaluation,
            EvaluationStatusInfo finalEvaluation,
            ProgressTimelineInfo progress
    ) {
    }

    public record EvaluationStatusInfo(
            String status, // "PASS" | "FAIL" | "PENDING" | "NONE"
            String label
    ) {
    }

    public record ProgressTimelineInfo(
            String currentStep,
            List<ProgressStepInfo> steps,
            LocalDate resultAnnounceAt
    ) {
    }

    public record ProgressStepInfo(
            String step,
            String label,
            boolean done,
            boolean active
    ) {
    }

    public record MyApplicationCardInfo(
            Long recruitmentId,
            Long applicationId,
            Long formResponseId,
            String recruitmentTitle,
            String badge, // "SUBMITTED" | "PAST"
            ApplicationStatus status,
            Instant submittedAt
    ) {
    }

}
