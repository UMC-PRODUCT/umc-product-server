package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewQuery;

public interface GetInterviewEvaluationViewUseCase {
    GetInterviewEvaluationViewInfo get(GetInterviewEvaluationViewQuery query);
}
