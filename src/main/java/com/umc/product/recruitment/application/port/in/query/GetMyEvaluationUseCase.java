package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyEvaluationQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyEvaluationInfo;

public interface GetMyEvaluationUseCase {
    MyEvaluationInfo get(GetMyEvaluationQuery query);
}
