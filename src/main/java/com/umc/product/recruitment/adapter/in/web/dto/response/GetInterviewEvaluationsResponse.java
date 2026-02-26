package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationsInfo;
import java.util.List;

public record GetInterviewEvaluationsResponse(
    Double avgScore,
    List<InterviewEvaluationResponse> interviewEvaluationSummaries
) {
    public static GetInterviewEvaluationsResponse from(GetInterviewEvaluationsInfo info) {
        return new GetInterviewEvaluationsResponse(
            info.avgScore(),
            info.items().stream().map(InterviewEvaluationResponse::from).toList()
        );
    }

    public record InterviewEvaluationResponse(
        Evaluator evaluator,
        Integer score,
        String comments
    ) {
        public static InterviewEvaluationResponse from(
            GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo i) {
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
