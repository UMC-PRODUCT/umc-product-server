package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import java.time.Instant;

public record GetMyDocumentEvaluationResponse(
    MyDocumentEvaluationResponse myEvaluation
) {

    public static GetMyDocumentEvaluationResponse from(GetMyDocumentEvaluationInfo info) {
        if (info == null || info.myEvaluation() == null) {
            return new GetMyDocumentEvaluationResponse(null);
        }
        var e = info.myEvaluation();
        return new GetMyDocumentEvaluationResponse(
            new MyDocumentEvaluationResponse(
                e.applicationId(),
                e.evaluationId(),
                e.score(),
                e.comments(),
                e.submitted(),
                e.savedAt()
            )
        );
    }


    public record MyDocumentEvaluationResponse(
        Long applicationId,
        Long evaluationId,
        Integer score,
        String comments,
        boolean submitted,
        Instant savedAt
    ) {
    }
}
