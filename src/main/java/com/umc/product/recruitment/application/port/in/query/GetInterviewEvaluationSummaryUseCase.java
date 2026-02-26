package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationsInfo;

public interface GetInterviewEvaluationSummaryUseCase {
    GetInterviewEvaluationsInfo get(GetInterviewEvaluationSummaryQuery query);
}
