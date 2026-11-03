package com.umc.product.report.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.report.application.port.in.command.dto.ReportMemberResult;
import com.umc.product.report.domain.enums.ReportReason;

/**
 * 사용자 신고 접수 응답입니다. ID는 문자열로 내려 JavaScript 정밀도 손실을 막습니다.
 */
public record ReportMemberReceiptResponse(
    String reportId,
    String memberId,
    List<ReportReason> reasons,
    Instant createdAt
) {

    public static ReportMemberReceiptResponse from(ReportMemberResult result) {
        return new ReportMemberReceiptResponse(
            String.valueOf(result.reportId()),
            String.valueOf(result.memberId()),
            result.reasons(),
            result.createdAt()
        );
    }
}
