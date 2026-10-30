package com.umc.product.report.adapter.in.web.dto;

import java.util.List;

import com.umc.product.report.application.port.in.command.dto.RedeliverReportsResult;

public record ReportRedeliveryResponse(int requeuedCount, List<Long> reportIds) {

    public static ReportRedeliveryResponse from(RedeliverReportsResult result) {
        return new ReportRedeliveryResponse(result.count(), result.reportIds());
    }
}
