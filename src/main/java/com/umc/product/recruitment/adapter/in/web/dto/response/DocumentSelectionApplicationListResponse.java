package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.global.response.PageResponse;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import java.util.List;

public record DocumentSelectionApplicationListResponse(
        DocumentSelectionSummary summary,
        String sort,
        PageResponse<DocumentSelectionApplicationResponse> documentSelectionApplications
) {
    public static DocumentSelectionApplicationListResponse from(DocumentSelectionApplicationListInfo info) {
        return new DocumentSelectionApplicationListResponse(
                new DocumentSelectionSummary(
                        info.summary().totalCount(),
                        info.summary().selectedCount()
                ),
                info.sort(),
                new PageResponse<>(
                        info.documentSelectionApplications().stream()
                                .map(DocumentSelectionApplicationResponse::from)
                                .toList(),
                        info.pagination().page(),
                        info.pagination().size(),
                        info.pagination().totalElements(),
                        info.pagination().totalPages(),
                        info.pagination().hasNext(),
                        info.pagination().hasPrevious()
                )
        );
    }

    public record DocumentSelectionSummary(
            long totalCount,
            long selectedCount
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
}
