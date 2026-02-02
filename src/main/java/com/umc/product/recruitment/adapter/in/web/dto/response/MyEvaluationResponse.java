package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.MyEvaluationInfo;
import java.time.Instant;

public record MyEvaluationResponse(
        Long applicationId,
        Long evaluationId,
        Integer score,
        String comment,
        Instant updatedAt
) {
    public static MyEvaluationResponse from(MyEvaluationInfo info) {
        return new MyEvaluationResponse(
                info.applicationId(),
                info.evaluationId(),
                info.score(),
                info.comment(),
                info.updatedAt()
        );

    }
}
