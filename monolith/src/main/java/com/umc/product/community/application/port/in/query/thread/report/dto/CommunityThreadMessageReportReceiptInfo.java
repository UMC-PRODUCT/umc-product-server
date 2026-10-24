package com.umc.product.community.application.port.in.query.thread.report.dto;

import java.time.Instant;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityThreadMessageReportReceiptInfo(
    Long reportId,
    Long messageId,
    ReportReason reason,
    Instant createdAt
) {

    public static CommunityThreadMessageReportReceiptInfo from(
        Report report,
        Long messageId,
        ReportReason reason
    ) {
        return new CommunityThreadMessageReportReceiptInfo(
            report.getId(),
            messageId,
            reason,
            report.getCreatedAt()
        );
    }
}
