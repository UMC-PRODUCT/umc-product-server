package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsQuery;

public interface GetLiveQuestionsUseCase {
    GetLiveQuestionsInfo get(GetLiveQuestionsQuery query);
}
