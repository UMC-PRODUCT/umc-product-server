package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo.ByPart;
import java.util.List;
import java.util.Map;

public record FinalSelectionApplicationListResponse(
        FinalSelectionSummary summary,
        List<FinalSelectionApplicationResponse> finalSelectionApplications,
        PaginationResponse pagination
) {
    public static FinalSelectionApplicationListResponse from(FinalSelectionApplicationListInfo info) {
        return new FinalSelectionApplicationListResponse(
                new FinalSelectionSummary(
                        info.summary().totalCount(),
                        info.summary().selectedCount(),
                        info.summary().byPart()
                ),
                info.finalSelectionApplications().stream().map(FinalSelectionApplicationResponse::from).toList(),
                new PaginationResponse(
                        info.pagination().page(),
                        info.pagination().size(),
                        info.pagination().totalPages(),
                        info.pagination().totalElements()
                )
        );
    }

    public record FinalSelectionSummary(
            long totalCount,
            long selectedCount,
            Map<String, ByPart> byPart
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
                    new Selection(info.selection().status(),
                            new PartResponse(
                                    info.selection().part().name(),
                                    info.selection().part().getLabel()
                            )
                    )
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

    public record PaginationResponse(
            int page,
            int size,
            int totalPages,
            long totalElements
    ) {
    }
}