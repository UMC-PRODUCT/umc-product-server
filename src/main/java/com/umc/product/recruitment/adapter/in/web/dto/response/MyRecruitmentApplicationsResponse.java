package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationProgressNoticeType;
import com.umc.product.recruitment.application.port.in.query.dto.EvaluationStatusCode;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record MyRecruitmentApplicationsResponse(
        String nickName,
        String name,
        CurrentApplicationStatusResponse current,
        List<MyApplicationCardResponse> applications
) {
    public static MyRecruitmentApplicationsResponse from(MyApplicationListInfo info) {
        return new MyRecruitmentApplicationsResponse(
                info.nickName(),
                info.name(),
                info.current() == null ? null : CurrentApplicationStatusResponse.from(info.current()),
                info.applications().stream().map(MyApplicationCardResponse::from).toList()
        );
    }

    public record CurrentApplicationStatusResponse(
            List<String> appliedParts,
            EvaluationStatusForApplicantResponse documentEvaluation,
            EvaluationStatusForApplicantResponse finalEvaluation,
            ProgressResponse progress
    ) {
        public static CurrentApplicationStatusResponse from(MyApplicationListInfo.CurrentApplicationStatusInfo info) {
            return new CurrentApplicationStatusResponse(
                    info.appliedParts(),
                    info.documentEvaluation() == null ? null
                            : EvaluationStatusForApplicantResponse.from(info.documentEvaluation()),
                    info.finalEvaluation() == null ? null
                            : EvaluationStatusForApplicantResponse.from(info.finalEvaluation()),
                    info.progress() == null ? null : ProgressResponse.from(info.progress())
            );
        }
    }

    public record EvaluationStatusForApplicantResponse(
            EvaluationStatusCode status
    ) {
        public static EvaluationStatusForApplicantResponse from(MyApplicationListInfo.EvaluationStatusInfo info) {
            return new EvaluationStatusForApplicantResponse(info.status());
        }
    }

    public record ProgressResponse(
            String currentStep,
            List<ProgressStepResponse> steps,
            ApplicationProgressNoticeType noticeType,
            LocalDate noticeDate,
            Integer nextRecruitmentMonth
    ) {
        public static ProgressResponse from(MyApplicationListInfo.ProgressTimelineInfo info) {
            return new ProgressResponse(
                    info.currentStep(),
                    info.steps().stream().map(ProgressStepResponse::from).toList(),
                    info.noticeType(),
                    info.noticeDate(),
                    info.nextRecruitmentMonth()
            );
        }
    }

    public record ProgressStepResponse(
            String step,
            String label,
            boolean done,
            boolean active
    ) {
        public static ProgressStepResponse from(MyApplicationListInfo.ProgressStepInfo info) {
            return new ProgressStepResponse(info.step(), info.label(), info.done(), info.active());
        }
    }

    public record MyApplicationCardResponse(
            Long recruitmentId,
            Long formResponseId,
            Long applicationId,
            String recruitmentTitle,
            String badge,
            ApplicationStatus status,
            Instant submittedAt
    ) {
        public static MyApplicationCardResponse from(MyApplicationListInfo.MyApplicationCardInfo info) {
            return new MyApplicationCardResponse(
                    info.recruitmentId(),
                    info.formResponseId(),
                    info.applicationId(),
                    info.recruitmentTitle(),
                    info.badge(),
                    info.status(),
                    info.submittedAt()
            );
        }
    }
}
