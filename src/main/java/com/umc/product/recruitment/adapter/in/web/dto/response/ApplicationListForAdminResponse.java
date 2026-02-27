package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.global.response.PageResponse;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record ApplicationListForAdminResponse(
    Filters filters,
    PageResponse<ApplicationForAdminResponse> applications
) {
    public static ApplicationListForAdminResponse from(ApplicationListForAdminInfo info) {
        return new ApplicationListForAdminResponse(
            new Filters(
                info.filters().chapterId(),
                info.filters().schoolId(),
                info.filters().part(),
                info.filters().keyword()
            ),
            new PageResponse<>(
                info.applications().stream()
                    .map(ApplicationForAdminResponse::from)
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

    public record Filters(
        Long chapterId,
        Long schoolId,
        String part,
        String keyword
    ) {
    }

    public record ApplicationForAdminResponse(
        Long applicationId,
        ApplicantInfo applicant,
        SchoolInfo school,
        List<AppliedPartInfo> appliedParts,
        FinalResultInfo finalResult
    ) {
        public static ApplicationForAdminResponse from(
            ApplicationListForAdminInfo.ApplicationForAdminInfo application) {
            return new ApplicationForAdminResponse(
                application.applicationId(),
                new ApplicantInfo(application.applicant().nickname(), application.applicant().name()),
                new SchoolInfo(application.school().schoolId(), application.school().name()),
                application.appliedParts().stream()
                    .map(p -> new AppliedPartInfo(p.priority(), PartResponse.from(p.part())))
                    .toList(),
                FinalResultInfo.from(application.finalResult())
            );
        }
    }

    public record ApplicantInfo(String nickname, String name) {
    }

    public record SchoolInfo(Long schoolId, String name) {
    }

    public record AppliedPartInfo(int priority, PartResponse part) {
    }

    public record FinalResultInfo(String status, PartResponse selectedPart) {
        public static FinalResultInfo from(ApplicationListForAdminInfo.FinalResult r) {
            return new FinalResultInfo(
                r.status(),
                PartResponse.from(r.selectedPart()) // null이면 null 반환
            );
        }
    }

    public record PartResponse(String key, String label) {
        public static PartResponse from(PartKey partKey) {
            if (partKey == null) {
                return null;
            }
            return new PartResponse(partKey.name(), partKey.getLabel());
        }
    }
}
