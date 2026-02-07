package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;

public record GetInterviewSheetQuestionsQuery(
    Long recruitmentId,
    PartKey partKey,
    Long requesterMemberId
) {
}
