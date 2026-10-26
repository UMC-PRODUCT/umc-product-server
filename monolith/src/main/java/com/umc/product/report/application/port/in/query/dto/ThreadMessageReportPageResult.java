package com.umc.product.report.application.port.in.query.dto;

import java.util.List;

public record ThreadMessageReportPageResult(List<ReportInfo> reports, long total) {

    public ThreadMessageReportPageResult {
        reports = reports == null ? List.of() : List.copyOf(reports);
    }
}
