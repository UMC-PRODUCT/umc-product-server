package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewInfo;
import java.time.Instant;
import java.util.List;

public record GetInterviewEvaluationViewResponse(
    Long assignmentId,
    Long applicationId,
    ApplicationResponse application,
    InterviewQuestionSheetResponse questions,
    LiveEvaluationListResponse liveEvaluations,
    MyInterviewEvaluationResponse myEvaluation
) {
    public static GetInterviewEvaluationViewResponse from(GetInterviewEvaluationViewInfo info) {
        return new GetInterviewEvaluationViewResponse(
            info.assignmentId(),
            info.applicationId(),
            ApplicationResponse.from(info.application()),
            InterviewQuestionSheetResponse.from(info.questions()),
            LiveEvaluationListResponse.from(info.liveEvaluations()),
            MyInterviewEvaluationResponse.fromNullable(info.myEvaluation())
        );
    }

    public record ApplicationResponse(
        Applicant applicant,
        List<AppliedPart> appliedParts
    ) {
        public static ApplicationResponse from(GetInterviewEvaluationViewInfo.ApplicationInfo a) {
            return new ApplicationResponse(
                new Applicant(a.applicant().nickname(), a.applicant().name()),
                a.appliedParts().stream()
                    .map(p -> new AppliedPart(p.priority(), p.key(), p.label()))
                    .toList()
            );
        }
    }

    public record Applicant(String nickname, String name) {
    }

    public record AppliedPart(Integer priority, String key, String label) {
    }

    public record InterviewQuestionSheetResponse(
        List<InterviewQuestionResponse> common,
        List<InterviewQuestionResponse> firstChoice,
        List<InterviewQuestionResponse> secondChoice,
        List<LiveQuestionResponse> live
    ) {
        public static InterviewQuestionSheetResponse from(GetInterviewEvaluationViewInfo.InterviewQuestionSheetInfo q) {
            return new InterviewQuestionSheetResponse(
                q.common().stream().map(InterviewQuestionResponse::from).toList(),
                q.firstChoice().stream().map(InterviewQuestionResponse::from).toList(),
                q.secondChoice().stream().map(InterviewQuestionResponse::from).toList(),
                q.live().stream().map(LiveQuestionResponse::from).toList()
            );
        }
    }

    public record InterviewQuestionResponse(Long questionId, Integer orderNo, String text) {
        public static InterviewQuestionResponse from(GetInterviewEvaluationViewInfo.InterviewQuestionInfo q) {
            return new InterviewQuestionResponse(q.questionId(), q.orderNo(), q.text());
        }
    }

    public record LiveQuestionResponse(
        Long liveQuestionId,
        Integer orderNo,
        String text,
        CreatedBy createdBy,
        Boolean canEdit
    ) {
        public static LiveQuestionResponse from(GetInterviewEvaluationViewInfo.LiveQuestionInfo q) {
            return new LiveQuestionResponse(
                q.liveQuestionId(),
                q.orderNo(),
                q.text(),
                new CreatedBy(q.createdBy().memberId(), q.createdBy().nickname(), q.createdBy().name()),
                q.canEdit()
            );
        }
    }

    public record CreatedBy(Long memberId, String nickname, String name) {
    }

    public record LiveEvaluationListResponse(
        Double avgScore,
        List<InterviewEvaluationResponse> items
    ) {
        public static LiveEvaluationListResponse from(
            GetInterviewEvaluationViewInfo.LiveEvaluationListInfo e) {
            return new LiveEvaluationListResponse(
                e.avgScore(),
                e.items().stream().map(InterviewEvaluationResponse::from).toList()
            );
        }

        public record InterviewEvaluationResponse(Evaluator evaluator, Integer score, String comments) {
            public static InterviewEvaluationResponse from(GetInterviewEvaluationViewInfo.LiveEvaluationItem i) {
                return new InterviewEvaluationResponse(
                    new Evaluator(i.evaluator().memberId(), i.evaluator().nickname(), i.evaluator().name()),
                    i.score(),
                    i.comments()
                );
            }
        }

        public record Evaluator(Long memberId, String nickname, String name) {
        }
    }

    public record MyInterviewEvaluationResponse(
        Long evaluationId,
        Integer score,
        String comments,
        Instant submittedAt
    ) {
        public static MyInterviewEvaluationResponse fromNullable(
            GetInterviewEvaluationViewInfo.MyInterviewEvaluationInfo e) {
            if (e == null) {
                return null;
            }
            return new MyInterviewEvaluationResponse(e.evaluationId(), e.score(), e.comments(), e.submittedAt());
        }
    }
}
