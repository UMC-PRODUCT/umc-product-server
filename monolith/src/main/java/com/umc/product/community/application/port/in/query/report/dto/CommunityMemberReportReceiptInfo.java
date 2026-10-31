package com.umc.product.community.application.port.in.query.report.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.enums.ReportReason;

public record CommunityMemberReportReceiptInfo(
    Long reportId,
    Long memberId,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static CommunityMemberReportReceiptInfo from(
        SubmitReportResult submitted,
        Long memberId
    ) {
        return new CommunityMemberReportReceiptInfo(
            submitted.reportId(),
            memberId,
            submitted.reasons(),
            submitted.createdAt()
        );
    }
}
