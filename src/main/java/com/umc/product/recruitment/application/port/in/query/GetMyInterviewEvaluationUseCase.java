package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationQuery;

public interface GetMyInterviewEvaluationUseCase {
    GetMyInterviewEvaluationInfo get(GetMyInterviewEvaluationQuery query);
}
