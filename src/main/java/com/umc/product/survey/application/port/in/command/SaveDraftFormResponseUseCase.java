package com.umc.product.survey.application.port.in.command;

public interface SaveDraftFormResponseUseCase {
    Long saveDraft(SubmitFormResponseCommand command);
}
