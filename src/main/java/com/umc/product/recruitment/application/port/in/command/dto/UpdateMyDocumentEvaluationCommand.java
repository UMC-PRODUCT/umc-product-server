package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateMyDocumentEvaluationCommand(
    Long recruitmentId,
    Long applicationId,
    Long evaluatorMemberId,
    Integer score,
    String comments
) {
}
