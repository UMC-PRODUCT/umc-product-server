package com.umc.product.report.application.port.in.command.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportReason;

public record SubmitReportResult(Long reportId, List<ReportReason> reasons, Instant createdAt) {

    public static SubmitReportResult from(Report report) {
        return new SubmitReportResult(report.getId(), report.getReasonCodes(), report.getCreatedAt());
    }
}
