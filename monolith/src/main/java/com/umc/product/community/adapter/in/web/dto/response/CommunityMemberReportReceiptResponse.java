package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;
import java.util.List;

import com.umc.product.community.application.port.in.query.report.dto.CommunityMemberReportReceiptInfo;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityMemberReportReceiptResponse(
    String reportId,
    String memberId,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static CommunityMemberReportReceiptResponse from(CommunityMemberReportReceiptInfo info) {
        return new CommunityMemberReportReceiptResponse(
            text(info.reportId()),
            text(info.memberId()),
            info.reasons(),
            info.createdAt()
        );
    }
}
