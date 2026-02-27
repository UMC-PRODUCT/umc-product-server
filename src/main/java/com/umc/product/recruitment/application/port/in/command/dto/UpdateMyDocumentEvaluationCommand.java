package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.enums.DocumentEvaluationAction;

public record UpdateMyDocumentEvaluationCommand(
    Long recruitmentId,
    Long applicationId,
    Long evaluatorMemberId,
    DocumentEvaluationAction action,
    Integer score,
    String comments
) {
    public boolean isSubmit() {
        return action == DocumentEvaluationAction.SUBMIT;
    }
}
