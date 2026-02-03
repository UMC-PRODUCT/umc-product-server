package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.in.web.dto.request.EvaluationDecision;

public record UpdateFinalStatusCommand(
        Long recruitmentId,
        Long applicationId,
        EvaluationDecision evaluationDecision,        // WAITING | PASSED
        ChallengerPart selectedPart,  // status=PASSED일 때 필수
        Long requesterId
) {
}
