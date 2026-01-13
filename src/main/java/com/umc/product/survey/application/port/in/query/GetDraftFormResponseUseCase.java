package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.DraftFormResponseInfo;
import com.umc.product.survey.application.port.in.query.dto.GetDraftFormResponseQuery;

public interface GetDraftFormResponseUseCase {
    DraftFormResponseInfo getDraft(GetDraftFormResponseQuery query);
}
