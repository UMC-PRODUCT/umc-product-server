package com.umc.product.report.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

public record ReportInfo(
    Long reportId,
    Long reporterId,
    ReportTargetType targetType,
    Long targetId,
    Long threadId,
    ReportReason reasonCode,
    List<ReportReason> reasons,
    ReportStatus status,
    Instant createdAt
) {

    public static ReportInfo from(Report report) {
        return new ReportInfo(
            report.getId(),
            report.getReporterId(),
            report.getTargetType(),
            report.getTargetId(),
            report.getThreadId(),
            report.getReasonCode(),
            report.getReasonCodes(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }
}
