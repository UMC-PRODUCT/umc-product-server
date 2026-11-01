package com.umc.product.report.application.port.in.command.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.domain.enums.ReportReason;

/**
 * 사용자 신고 접수 결과입니다.
 */
public record ReportMemberResult(
    Long reportId,
    Long memberId,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static ReportMemberResult of(SubmitReportResult submitted, Long memberId) {
        return new ReportMemberResult(submitted.reportId(), memberId, submitted.reasons(), submitted.createdAt());
    }
}
