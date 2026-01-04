package com.umc.product.form.application.port.in.command;

public interface SaveDraftFormResponseUseCase {
    Long saveDraft(SubmitFormResponseCommand command);
}
