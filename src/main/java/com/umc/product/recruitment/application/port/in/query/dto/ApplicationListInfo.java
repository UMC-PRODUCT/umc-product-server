package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.util.List;

public record ApplicationListInfo(
    Long recruitmentId,
    SummaryInfo summary,
    List<ApplicationSummary> applicationSummaries,
    PaginationInfo pagination
) {
    public record SummaryInfo(
        long totalCount,
        long evaluatedCount
    ) {
    }

    public record ApplicationSummary(
        Long applicationId,
        Long applicantMemberId,
        String applicantName,
        String applicantNickname,
        List<PreferredPartInfo> preferredParts,
        boolean isEvaluated
    ) {
    }

    public record PreferredPartInfo(
        Integer priority,
        PartOption part
    ) {
    }

    public record PaginationInfo(
        int page,
        int size,
        int totalPages,
        long totalElements
    ) {
    }
}
