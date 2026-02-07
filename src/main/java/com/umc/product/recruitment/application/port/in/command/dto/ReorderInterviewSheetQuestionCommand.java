package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record ReorderInterviewSheetQuestionCommand(
    Long recruitmentId,
    PartKey partKey,
    List<Long> orderedQuestionIds,
    Long requesterMemberId
) {
}
