package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;

public interface GetApplicationEvaluationListUseCase {
    ApplicationEvaluationListInfo get(GetApplicationEvaluationListQuery query);
}
