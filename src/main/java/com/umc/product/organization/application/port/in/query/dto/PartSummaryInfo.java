package com.umc.product.organization.application.port.in.query.dto;

import java.util.List;

public record PartSummaryInfo(
        Long schoolId,
        String schoolName,
        List<PartInfo> parts
) {
}
