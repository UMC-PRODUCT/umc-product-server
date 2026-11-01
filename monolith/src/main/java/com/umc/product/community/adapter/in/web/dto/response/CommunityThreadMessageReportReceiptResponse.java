package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;
import java.util.List;

import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageReportReceiptInfo;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityThreadMessageReportReceiptResponse(
    String reportId,
    String messageId,
    ReportReason reason,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static CommunityThreadMessageReportReceiptResponse from(
        CommunityThreadMessageReportReceiptInfo info
    ) {
        return new CommunityThreadMessageReportReceiptResponse(
            text(info.reportId()),
            text(info.messageId()),
            info.reason(),
            info.reasons(),
            info.createdAt()
        );
    }
}
