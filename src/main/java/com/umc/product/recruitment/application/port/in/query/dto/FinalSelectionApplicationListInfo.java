package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record FinalSelectionApplicationListInfo(
    Summary summary,
    String sort, // "SCORE_DESC" / "SCORE_ASC" / "EVALUATED_AT_ASC"
    List<FinalSelectionApplicationInfo> finalSelectionApplications,
    PaginationInfo pagination
) {
    public record Summary(
        long totalCount,
        long selectedCount
    ) {
    }

    public record FinalSelectionApplicationInfo(
        Long applicationId,
        ApplicantInfo applicant,
        List<AppliedPartInfo> appliedParts,
        Double documentScore,
        Double interviewScore,
        Double finalScore,
        Selection selection
    ) {
    }

    public record ApplicantInfo(
        String nickname,
        String name
    ) {
    }

    public record AppliedPartInfo(
        int priority,
        PartKey part
    ) {
    }

    public record Selection(
        String status, // PASS | FAIL | WAIT
        PartKey part   // PASS일 때만 있을 수 있음 (nullable)
    ) {
    }

    public record PaginationInfo(
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
    ) {
    }
}
