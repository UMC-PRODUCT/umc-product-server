package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.adapter.in.web.dto.request.EvaluationDecision;

public record UpdateDocumentStatusCommand(
        Long recruitmentId,
        Long applicationId,
        EvaluationDecision decision,
        Long requesterMemberId
) {
}
