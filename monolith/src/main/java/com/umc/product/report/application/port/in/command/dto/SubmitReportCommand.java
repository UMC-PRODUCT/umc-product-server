package com.umc.product.report.application.port.in.command.dto;

import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;

public record SubmitReportCommand(
    Long reporterId,
    ReportTargetType targetType,
    Long targetId,
    Long threadId,
    ReportReason reasonCode
) {

    public static SubmitReportCommand of(Long reporterId, ReportTargetType targetType, Long targetId) {
        return new SubmitReportCommand(reporterId, targetType, targetId, null, null);
    }

    public static SubmitReportCommand threadMessage(
        Long reporterId,
        Long threadId,
        Long messageId,
        ReportReason reasonCode
    ) {
        return new SubmitReportCommand(reporterId, ReportTargetType.THREAD_MESSAGE, messageId, threadId, reasonCode);
    }
}
