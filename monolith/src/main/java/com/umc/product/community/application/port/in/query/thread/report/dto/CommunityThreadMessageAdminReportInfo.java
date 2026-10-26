package com.umc.product.community.application.port.in.query.thread.report.dto;

import java.time.Instant;

import com.umc.product.report.application.port.in.query.dto.ReportInfo;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;

public record CommunityThreadMessageAdminReportInfo(
    Long reportId,
    Long threadId,
    Long messageId,
    ReportReason reason,
    ReportStatus status,
    Long reporterId,
    Instant createdAt
) {

    public static CommunityThreadMessageAdminReportInfo from(ReportInfo report) {
        return new CommunityThreadMessageAdminReportInfo(
            report.reportId(),
            report.threadId(),
            report.targetId(),
            report.reasonCode(),
            report.status(),
            report.reporterId(),
            report.createdAt()
        );
    }
}
