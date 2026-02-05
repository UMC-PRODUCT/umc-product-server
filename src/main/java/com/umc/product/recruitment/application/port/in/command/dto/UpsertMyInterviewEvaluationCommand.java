package com.umc.product.recruitment.application.port.in.command.dto;

public record UpsertMyInterviewEvaluationCommand(
        Long recruitmentId,
        Long assignmentId,
        Long evaluatorMemberId,
        Integer score,
        String comments
) {
}
