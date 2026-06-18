package com.umc.product.feedback.application.port.in.command.dto;

import java.util.List;

import com.umc.product.survey.application.port.in.command.dto.AnswerCommand;

import lombok.Builder;

@Builder
public record SubmitUserFeedbackResponseCommand(
    Long templateId,
    Long respondentMemberId,
    List<AnswerCommand> answers
) {
}
