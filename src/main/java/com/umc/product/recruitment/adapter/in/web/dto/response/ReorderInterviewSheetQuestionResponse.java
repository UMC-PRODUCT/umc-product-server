package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionResult;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record ReorderInterviewSheetQuestionResponse(
    PartKey partKey,
    List<Long> orderedQuestionIds
) {
    public static ReorderInterviewSheetQuestionResponse from(ReorderInterviewSheetQuestionResult result) {
        return new ReorderInterviewSheetQuestionResponse(result.partKey(), result.orderedQuestionIds());
    }
}
