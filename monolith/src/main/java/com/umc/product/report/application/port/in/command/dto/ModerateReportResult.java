package com.umc.product.report.application.port.in.command.dto;

import java.time.Instant;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;

public record ModerateReportResult(
    Long reportId,
    ReportStatus status,
    ReportActionType actionType,
    ReportActionReason reasonCode,
    String memo,
    ReportTargetRemoval targetRemoval,
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
            action.getTargetRemoval(),
            action.getModeratorName(),
            action.getActedAt()
        );
    }
}
