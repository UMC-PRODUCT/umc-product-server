package com.umc.product.community.application.port.in.query.thread.report.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityThreadMessageReportReceiptInfo(
    Long reportId,
    Long messageId,
    ReportReason reason,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static CommunityThreadMessageReportReceiptInfo from(
        SubmitReportResult submitted,
        Long messageId
    ) {
        return new CommunityThreadMessageReportReceiptInfo(
            submitted.reportId(),
            messageId,
            submitted.reasons().get(0),
            submitted.reasons(),
            submitted.createdAt()
        );
    }
}
