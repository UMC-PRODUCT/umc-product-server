package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.global.response.PageResponse;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import java.util.List;

public record FinalSelectionApplicationListResponse(
    FinalSelectionSummary summary,
    String sort,
    PageResponse<FinalSelectionApplicationResponse> finalSelectionApplications
) {
    public static FinalSelectionApplicationListResponse from(FinalSelectionApplicationListInfo info) {
        return new FinalSelectionApplicationListResponse(
            new FinalSelectionSummary(
                info.summary().totalCount(),
                info.summary().selectedCount()
            ),
            info.sort(),
            new PageResponse<>(
                info.finalSelectionApplications().stream()
                    .map(FinalSelectionApplicationResponse::from)
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

    public record FinalSelectionSummary(
        long totalCount,
        long selectedCount
    ) {
    }

    public record FinalSelectionApplicationResponse(
        Long applicationId,
        ApplicantResponse applicant,
        List<AppliedPartResponse> appliedParts,
        Double documentScore,
        Double interviewScore,
        Double finalScore,
        Selection selection
    ) {
        public static FinalSelectionApplicationResponse from(
            FinalSelectionApplicationListInfo.FinalSelectionApplicationInfo info) {

            FinalSelectionApplicationListInfo.Selection sel = info.selection();

            PartResponse selectedPart = (sel.part() == null)
                ? null
                : new PartResponse(sel.part().name(), sel.part().getLabel());

            return new FinalSelectionApplicationResponse(
                info.applicationId(),
                new ApplicantResponse(info.applicant().nickname(), info.applicant().name()),
                info.appliedParts().stream()
                    .map(p -> new AppliedPartResponse(
                        p.priority(),
                        new PartResponse(
                            p.part().name(), p.part().getLabel())
                    )).toList(),
                info.documentScore(),
                info.interviewScore(),
                info.finalScore(),
                new Selection(sel.status(), selectedPart)
            );
        }
    }

    public record ApplicantResponse(String nickname, String name) {
    }

    public record AppliedPartResponse(int priority, PartResponse part) {
    }

    public record Selection(String status, PartResponse selectedPart) {
    }

    public record PartResponse(String key, String label) {
    }
}
