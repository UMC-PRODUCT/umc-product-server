package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationQuery;

public interface GetMyDocumentEvaluationUseCase {
    GetMyDocumentEvaluationInfo get(GetMyDocumentEvaluationQuery query);
}
