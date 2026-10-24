package com.umc.product.report.application.port.in.command.dto;

import java.time.Instant;

import com.umc.product.report.domain.Report;

public record SubmitReportResult(Long reportId, Instant createdAt) {

    public static SubmitReportResult from(Report report) {
        return new SubmitReportResult(report.getId(), report.getCreatedAt());
    }
}
