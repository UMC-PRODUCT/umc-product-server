package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecruitmentDashboardResponse(
    Long recruitmentId,
    ScheduleSummaryResponse scheduleSummary,
    ProgressResponse progress,
    ApplicationStatusResponse applicationStatus,
    EvaluationStatusResponse evaluationStatus
) {
    public static RecruitmentDashboardResponse from(RecruitmentDashboardInfo info) {
        return new RecruitmentDashboardResponse(
            info.recruitmentId(),
            ScheduleSummaryResponse.from(info.scheduleSummary()),
            ProgressResponse.from(info.progress()),
            ApplicationStatusResponse.from(info.applicationStatus()),
            EvaluationStatusResponse.from(info.evaluationStatus())
        );
    }

    public record ScheduleSummaryResponse(
        String phaseTitle,
        Integer dDay,
        DateRangeResponse dateRange,
        TodayInterviewResponse todayInterview
    ) {
        public static ScheduleSummaryResponse from(RecruitmentDashboardInfo.ScheduleSummaryInfo info) {
            return new ScheduleSummaryResponse(
                info.phaseTitle(),
                info.dDay(),
                DateRangeResponse.from(info.dateRange()),
                TodayInterviewResponse.from(info.todayInterview())
            );
        }
    }

    public record TodayInterviewResponse(
        LocalTime interviewTime,
        String nickName,
        String name,
        String message // "면접 진행 기간이 아닙니다."
    ) {
        public static TodayInterviewResponse from(RecruitmentDashboardInfo.TodayInterviewInfo info) {
            return new TodayInterviewResponse(
                info.interviewTime(),
                info.nickName(),
                info.name(),
                info.message()
            );
        }
    }

    public record DateRangeResponse(LocalDate start, LocalDate end) {
        public static DateRangeResponse from(RecruitmentDashboardInfo.DateRangeInfo info) {
            return new DateRangeResponse(info.start(), info.end());
        }
    }

    public record ProgressResponse(
        String currentStep, // "DOCUMENT_REVIEW" ...
        List<ProgressStepResponse> steps, // ["10기 모집","서류 평가",...]
        LocalDate resultAnnounceAt
    ) {
        public static ProgressResponse from(RecruitmentDashboardInfo.ProgressInfo info) {
            return new ProgressResponse(
                info.currentStep(),
                info.steps().stream().map(ProgressStepResponse::from).toList(),
                info.documentResultAnnounceAt()
            );
        }
    }

    public record ProgressStepResponse(
        String step,
        String label,
        boolean done,
        boolean active // 현재 진행 중 단계
    ) {
        public static ProgressStepResponse from(RecruitmentDashboardInfo.ProgressStepInfo info) {
            return new ProgressStepResponse(info.step(), info.label(), info.done(), info.active());
        }
    }

    public record ApplicationStatusResponse(
        int totalApplicants,
        List<PartApplicantCountResponse> partCounts
    ) {
        public static ApplicationStatusResponse from(RecruitmentDashboardInfo.ApplicationStatusInfo info) {
            return new ApplicationStatusResponse(
                info.totalApplicants(),
                info.partCounts().stream().map(PartApplicantCountResponse::from).toList()
            );
        }
    }

    public record PartApplicantCountResponse(
        ChallengerPart part,
        int count
    ) {
        public static PartApplicantCountResponse from(RecruitmentDashboardInfo.PartApplicantCountInfo info) {
            return new PartApplicantCountResponse(info.part(), info.count());
        }
    }

    public record EvaluationStatusResponse(
        EvaluationProgressResponse documentEvaluation,
        EvaluationProgressResponse interviewEvaluation,
        List<PartEvaluationStatusResponse> partStatuses
    ) {
        public static EvaluationStatusResponse from(RecruitmentDashboardInfo.EvaluationStatusInfo info) {
            return new EvaluationStatusResponse(
                EvaluationProgressResponse.from(info.documentEvaluation()),
                EvaluationProgressResponse.from(info.interviewEvaluation()),
                info.partStatuses().stream().map(PartEvaluationStatusResponse::from).toList()
            );
        }
    }

    public record EvaluationProgressResponse(
        int progressRate,
        int completed,
        int total
    ) {
        public static EvaluationProgressResponse from(RecruitmentDashboardInfo.EvaluationProgressInfo info) {
            return new EvaluationProgressResponse(info.progressRate(), info.completed(), info.total());
        }
    }

    public record PartEvaluationStatusResponse(
        ChallengerPart part,
        String documentStatusText, // 예: "서류 평가 완료", "서류 평가 진행 중"
        String interviewStatusText // 예: "면접 평가 전", "면접 평가 진행 중", "면접 평가 완료"
    ) {
        public static PartEvaluationStatusResponse from(RecruitmentDashboardInfo.PartEvaluationStatusInfo info) {
            return new PartEvaluationStatusResponse(
                info.part(),
                info.documentStatusText(),
                info.interviewStatusText()
            );
        }
    }
}
