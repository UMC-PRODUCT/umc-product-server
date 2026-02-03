package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;

public record GetFinalSelectionApplicationListQuery(
        Long recruitmentId,
        PartOption part,
        SortOption sort,
        Long requesterId
) {
}
