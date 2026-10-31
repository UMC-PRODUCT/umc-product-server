package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;

import com.umc.product.community.application.port.in.query.report.dto.CommunityMemberReportReceiptInfo;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityMemberReportReceiptResponse(
    String reportId,
    String memberId,
    ReportReason reason,
    Instant createdAt
) {

    public static CommunityMemberReportReceiptResponse from(CommunityMemberReportReceiptInfo info) {
        return new CommunityMemberReportReceiptResponse(
            text(info.reportId()),
            text(info.memberId()),
            info.reason(),
            info.createdAt()
        );
    }
}
