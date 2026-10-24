package com.umc.product.report.application.port.in.query.dto;

import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;

public record SearchThreadMessageReportsQuery(
    ReportStatus status,
    ReportReason reason,
    Long threadId,
    Long reporterId,
    int offset,
    int limit
) {
}
