package com.umc.product.report.application.port.in.command.dto;

import java.util.List;

/**
 * @param reportIds 다시 전달하도록 outbox에 넣은 신고 ID
 */
public record RedeliverReportsResult(List<Long> reportIds) {

    public RedeliverReportsResult {
        reportIds = List.copyOf(reportIds);
    }

    public int count() {
        return reportIds.size();
    }
}
