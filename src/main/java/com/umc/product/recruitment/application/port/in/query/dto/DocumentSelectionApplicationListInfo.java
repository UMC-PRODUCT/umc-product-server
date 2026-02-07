package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Map;

public record DocumentSelectionApplicationListInfo(
    Summary summary,
    String sort, // "SCORE_DESC" / "SCORE_ASC" / "EVALUATED_AT_ASC"
    List<DocumentSelectionApplicationInfo> documentSelectionApplications,
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

    public record DocumentSelectionApplicationInfo(
        Long applicationId,
        ApplicantInfo applicant,
        List<AppliedPartInfo> appliedParts,
        Double documentScore,
        DocumentResult documentResult
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

    public record DocumentResult(
        String decision // PASS | WAIT
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
