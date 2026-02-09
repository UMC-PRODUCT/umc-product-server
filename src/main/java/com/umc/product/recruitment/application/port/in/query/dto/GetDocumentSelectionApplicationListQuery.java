package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;

public record GetDocumentSelectionApplicationListQuery(
    Long recruitmentId,
    PartOption part,
    SortOption sort,
    int page,
    int size,
    Long requesterMemberId
) {
}
