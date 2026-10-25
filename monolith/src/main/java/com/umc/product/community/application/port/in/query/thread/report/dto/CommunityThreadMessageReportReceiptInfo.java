package com.umc.product.community.application.port.in.query.thread.report.dto;

import java.time.Instant;

import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityThreadMessageReportReceiptInfo(
    Long reportId,
    Long messageId,
    ReportReason reason,
    Instant createdAt
) {

    public static CommunityThreadMessageReportReceiptInfo from(
        SubmitReportResult submitted,
        Long messageId,
        ReportReason reason
    ) {
        return new CommunityThreadMessageReportReceiptInfo(
            submitted.reportId(),
            messageId,
            reason,
            submitted.createdAt()
        );
    }
}
