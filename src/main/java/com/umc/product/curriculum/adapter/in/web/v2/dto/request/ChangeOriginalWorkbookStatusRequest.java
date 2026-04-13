package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import lombok.Builder;

@Builder
public record ChangeOriginalWorkbookStatusRequest(
    Long originalWorkbookId,
    OriginalWorkbookStatus status
) {
}
