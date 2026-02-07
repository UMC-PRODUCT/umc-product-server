package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Map;

public record FinalSelectionApplicationListInfo(
    Summary summary,
    List<FinalSelectionApplicationInfo> finalSelectionApplications,
    PaginationInfo pagination
) {
    public record Summary(
        long totalCount,
        long selectedCount,
        Map<String, ByPart> byPart
    ) {
    }

    public record ByPart(
        long total,
        long selected
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
        String status,
        PartKey part
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
