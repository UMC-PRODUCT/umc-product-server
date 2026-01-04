package com.umc.product.form.application.port.in.query;

public interface GetDraftFormResponseUseCase {
    DraftFormResponseInfo getDraft(GetDraftFormResponseQuery query);
}
