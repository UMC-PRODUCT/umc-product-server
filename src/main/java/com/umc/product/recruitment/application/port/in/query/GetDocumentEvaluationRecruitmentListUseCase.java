package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentEvaluationRecruitmentListQuery;

public interface GetDocumentEvaluationRecruitmentListUseCase {
    DocumentEvaluationRecruitmentListInfo get(GetDocumentEvaluationRecruitmentListQuery query);
}
