package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.ApplicationStatus;
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
            ApplicationEvaluationStatusCode status
    ) {
    }

    public record ProgressTimelineInfo(
            String currentStep, // ApplicationProgressStep.name()
            List<ProgressStepInfo> steps,
            ApplicationProgressNoticeType noticeType, // nullable
            LocalDate noticeDate, // nullable (APPLY/DOC/FINAL)
            Integer nextRecruitmentMonth // nullable (불합격 시 3 or 9)
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
            String badge, // "DRAFT" | "SUBMITTED" | "PAST"
            ApplicationStatus status,
            Instant submittedAt
    ) {
    }

}
