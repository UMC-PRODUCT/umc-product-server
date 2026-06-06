package com.umc.product.project.application.port.in.command.dto;

import com.umc.product.survey.application.port.in.command.dto.AnswerCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record SubmitUserFeedbackResponseCommand(
    Long templateId,
    Long respondentMemberId,
    List<AnswerCommand> answers
) {
}
