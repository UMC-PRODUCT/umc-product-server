package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.survey.application.port.in.query.dto.GetFormResponseQuery;

public interface GetFormResponseUseCase {
    FormResponseInfo get(GetFormResponseQuery query);
}
