package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import java.util.List;

public record ApplicationListResponse(
        Long recruitmentId,
        List<ApplicationSummaryResponse> applicationSummaries,
        PaginationResponse pagination
) {
    public static ApplicationListResponse from(ApplicationListInfo info) {
        return new ApplicationListResponse(
                info.recruitmentId(),
                info.applicationSummaries().stream().map(ApplicationSummaryResponse::from).toList(),
                new PaginationResponse(
                        info.pagination().page(),
                        info.pagination().size(),
                        info.pagination().totalPages(),
                        info.pagination().totalElements()
                )
        );
    }

    public record ApplicationSummaryResponse(
            Long applicationId,

            Long applicantMemberId,
            String applicantName,
            String applicantNickname,

            List<PreferredPartWithPriorityResponse> preferredParts,

            boolean isEvaluated
    ) {
        static ApplicationSummaryResponse from(ApplicationListInfo.ApplicationSummary summary) {
            return new ApplicationSummaryResponse(
                    summary.applicationId(),
                    summary.applicantMemberId(),
                    summary.applicantName(),
                    summary.applicantNickname(),
                    summary.preferredParts().stream().map(PreferredPartWithPriorityResponse::from).toList(),
                    summary.isEvaluated()
            );
        }
    }

    public record PreferredPartWithPriorityResponse(
            Integer priority,
            String part,
            String label
    ) {
        static PreferredPartWithPriorityResponse from(ApplicationListInfo.PreferredPartInfo p) {
            return new PreferredPartWithPriorityResponse(
                    p.priority(),
                    p.part().getCode(),
                    p.part().getLabel()
            );
        }
    }

    public record PaginationResponse(
            int page,
            int size,
            int totalPages,
            long totalElements
    ) {
    }
}
