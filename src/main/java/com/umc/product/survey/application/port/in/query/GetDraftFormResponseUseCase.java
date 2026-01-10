package com.umc.product.survey.application.port.in.query;

public interface GetDraftFormResponseUseCase {
    DraftFormResponseInfo getDraft(GetDraftFormResponseQuery query);
}
