package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo.DocEvaluationSummary;
import java.math.BigDecimal;
import java.util.List;

public record ApplicationEvaluationsResponse(
    Long recruitmentId,
    Long applicationId,
    BigDecimal avgDocScore,
    List<DocEvaluationSummaryResponse> docEvaluationSummaries
) {
    public static ApplicationEvaluationsResponse from(
        ApplicationEvaluationListInfo info
    ) {
        return new ApplicationEvaluationsResponse(
            info.recruitmentId(),
            info.applicationId(),
            info.avgDocScore(),
            info.docEvaluationSummaries().stream()
                .map(DocEvaluationSummaryResponse::from)
                .toList()
        );
    }

    public record DocEvaluationSummaryResponse(
        Long evaluatorMemberId,
        String evaluatorName,
        String evaluatorNickname,
        Integer score,
        String comments
    ) {
        static DocEvaluationSummaryResponse from(DocEvaluationSummary s) {
            return new DocEvaluationSummaryResponse(
                s.evaluationId(),
                s.evaluatorName(),
                s.evaluatorNickname(),
                s.score(),
                s.comments()
            );
        }
    }
}
