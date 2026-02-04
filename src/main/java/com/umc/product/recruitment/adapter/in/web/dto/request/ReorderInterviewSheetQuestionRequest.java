package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.domain.enums.PartKey;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReorderInterviewSheetQuestionRequest(
        @NotNull PartKey partKey,
        @NotNull List<Long> orderedQuestionIds
) {
}
