package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo.ByPart;
import java.util.List;
import java.util.Map;

public record DocumentSelectionApplicationListResponse(
    DocumentSelectionSummary summary,
    String sort,
    List<DocumentSelectionApplicationResponse> documentSelectionApplications,
    PaginationResponse pagination
) {
    public static DocumentSelectionApplicationListResponse from(DocumentSelectionApplicationListInfo info) {
        return new DocumentSelectionApplicationListResponse(
            new DocumentSelectionSummary(
                info.summary().totalCount(),
                info.summary().selectedCount(),
                info.summary().byPart()
            ),
            info.sort(),
            info.documentSelectionApplications().stream()
                .map(DocumentSelectionApplicationResponse::from)
                .toList(),
            new PaginationResponse(
                info.pagination().page(),
                info.pagination().size(),
                info.pagination().totalPages(),
                info.pagination().totalElements()
            )
        );
    }

    public record DocumentSelectionSummary(
        long totalCount,
        long selectedCount,
        Map<String, ByPart> byPart
    ) {
    }

    public record DocumentSelectionApplicationResponse(
        Long applicationId,
        ApplicantResponse applicant,
        List<AppliedPartResponse> appliedParts,
        Double documentScore,
        DocumentResult documentResult
    ) {
        public static DocumentSelectionApplicationResponse from(
            DocumentSelectionApplicationListInfo.DocumentSelectionApplicationInfo info
        ) {
            return new DocumentSelectionApplicationResponse(
                info.applicationId(),
                new ApplicantResponse(info.applicant().nickname(), info.applicant().name()),
                info.appliedParts().stream()
                    .map(p -> new AppliedPartResponse(
                        p.priority(),
                        new PartResponse(p.part().name(), p.part().getLabel())
                    ))
                    .toList(),
                info.documentScore(),
                new DocumentResult(info.documentResult().decision())
            );
        }
    }

    public record ApplicantResponse(String nickname, String name) {
    }

    public record AppliedPartResponse(int priority, PartResponse part) {
    }

    public record DocumentResult(String decision) {
    }

    public record PartResponse(String key, String label) {
    }

    public record PaginationResponse(
        int page,
        int size,
        int totalPages,
        long totalElements
    ) {
    }
}
