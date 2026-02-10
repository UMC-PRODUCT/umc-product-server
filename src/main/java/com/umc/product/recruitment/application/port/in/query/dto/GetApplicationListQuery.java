package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;

public record GetApplicationListQuery(
    Long recruitmentId,
    PartOption part,
    String keyword,
    int page,
    int size,
    Long requesterMemberId
) {
}
