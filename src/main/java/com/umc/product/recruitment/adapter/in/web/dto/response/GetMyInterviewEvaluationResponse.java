package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import java.time.Instant;

public record GetMyInterviewEvaluationResponse(
        MyInterviewEvaluationResponse myEvaluation
) {
    public static GetMyInterviewEvaluationResponse from(GetMyInterviewEvaluationInfo info) {
        if (info == null || info.myEvaluation() == null) {
            return new GetMyInterviewEvaluationResponse(null);
        }
        var e = info.myEvaluation();
        return new GetMyInterviewEvaluationResponse(
                new MyInterviewEvaluationResponse(e.evaluationId(), e.score(), e.comments(), e.submittedAt())
        );
    }

    public record MyInterviewEvaluationResponse(
            Long evaluationId,
            Integer score,
            String comments,
            Instant submittedAt
    ) {
    }
}