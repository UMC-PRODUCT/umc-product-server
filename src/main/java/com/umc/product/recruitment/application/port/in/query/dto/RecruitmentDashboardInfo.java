package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecruitmentDashboardInfo(
    Long recruitmentId,
    ScheduleSummaryInfo scheduleSummary,
    ProgressInfo progress,
    ApplicationStatusInfo applicationStatus,
    EvaluationStatusInfo evaluationStatus
) {
    public record ScheduleSummaryInfo(
        String phaseTitle,
        Integer dDay,
        DateRangeInfo dateRange,
        TodayInterviewInfo todayInterview
    ) {
    }

    public record TodayInterviewInfo(
        LocalTime interviewTime,
        String nickName,
        String name,
        String message
    ) {
    }

    public record DateRangeInfo(LocalDate start, LocalDate end) {
    }

    public record ProgressInfo(
        String currentStep,
        List<ProgressStepInfo> steps,
        LocalDate documentResultAnnounceAt
    ) {
    }

    public record ProgressStepInfo(
        String step,
        String label,
        boolean done,
        boolean active
    ) {
    }

    public record ApplicationStatusInfo(
        int totalApplicants,
        List<PartApplicantCountInfo> partCounts
    ) {
    }

    public record PartApplicantCountInfo(
        ChallengerPart part,
        int count
    ) {
    }

    public record EvaluationStatusInfo(
        EvaluationProgressInfo documentEvaluation,
        EvaluationProgressInfo interviewEvaluation,
        List<PartEvaluationStatusInfo> partStatuses
    ) {
    }

    public record EvaluationProgressInfo(
        int progressRate,
        int completed,
        int total
    ) {
    }

    public record PartEvaluationStatusInfo(
        ChallengerPart part,
        String documentStatusText,
        String interviewStatusText
    ) {
    }
}
