package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.EvalPhaseStatus;
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
        List<TodayInterviewInfo> todayInterviews
    ) {
    }

    public record TodayInterviewInfo(
        LocalTime interviewTime,
        String nickName,
        String name
    ) {
    }

    public record DateRangeInfo(LocalDate start, LocalDate end) {
    }

    public record ProgressInfo(
        String currentStep,
        List<ProgressStepInfo> steps,
        ApplicationProgressNoticeType noticeType,
        LocalDate noticeDate
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
        EvalPhaseStatus documentStatus,
        EvalPhaseStatus interviewStatus
    ) {
    }
}
