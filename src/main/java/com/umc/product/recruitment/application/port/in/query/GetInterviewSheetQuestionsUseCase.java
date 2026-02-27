package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsQuery;

public interface GetInterviewSheetQuestionsUseCase {
    GetInterviewSheetQuestionsInfo get(GetInterviewSheetQuestionsQuery query);
}
