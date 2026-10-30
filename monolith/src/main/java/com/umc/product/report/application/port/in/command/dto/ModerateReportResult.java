package com.umc.product.report.application.port.in.command.dto;

import java.time.Instant;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;

public record ModerateReportResult(
    Long reportId,
    ReportStatus status,
    ReportActionType actionType,
    ReportActionReason reasonCode,
    String memo,
    String moderatorName,
    Instant actedAt
) {

    public static ModerateReportResult of(Report report, ReportAction action) {
        return new ModerateReportResult(
            report.getId(),
            report.getStatus(),
            action.getActionType(),
            action.getReasonCode(),
            action.getMemo(),
            action.getModeratorName(),
            action.getActedAt()
        );
    }
}
