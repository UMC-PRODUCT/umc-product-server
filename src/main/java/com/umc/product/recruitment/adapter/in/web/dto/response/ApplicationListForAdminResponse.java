package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import java.util.List;

public record ApplicationListForAdminResponse(
    Filters filters,
    long applicantCount,
    PaginationResponse pagination,
    List<ApplicationForAdminResponse> applications
) {
    public static ApplicationListForAdminResponse from(ApplicationListForAdminInfo info) {
        return new ApplicationListForAdminResponse(
            new Filters(
                info.filters().chapterId(),
                info.filters().schoolId(),
                info.filters().part(),
                info.filters().keyword()
            ),
            info.applicantCount(),
            new PaginationResponse(
                info.pagination().page(),
                info.pagination().size(),
                info.pagination().totalPages(),
                info.pagination().totalElements()
            ),
            info.applications().stream().map(ApplicationForAdminResponse::from).toList()
        );
    }

    public record Filters(
        Long chapterId,
        Long schoolId,
        String part,
        String keyword
    ) {
    }

    public record PaginationResponse(
        int page,
        int size,
        int totalPages,
        long totalElements
    ) {
    }

    public record ApplicationForAdminResponse(
        Long applicationId,
        ApplicantInfo applicant,
        SchoolInfo school,
        List<AppliedPartInfo> appliedParts,
        EvaluationInfo documentEvaluation,
        EvaluationInfo interviewEvaluation,
        FinalResultInfo finalResult
    ) {
        public static ApplicationForAdminResponse from(
            ApplicationListForAdminInfo.ApplicationForAdminInfo application) {
            return new ApplicationForAdminResponse(
                application.applicationId(),
                new ApplicantInfo(application.applicant().nickname(), application.applicant().name()),
                new SchoolInfo(application.school().schoolId(), application.school().name()),
                application.appliedParts().stream()
                    .map(p -> new AppliedPartInfo(p.priority(), p.key(), p.label()))
                    .toList(),
                new EvaluationInfo(application.documentEvaluation().status(),
                    application.documentEvaluation().score()),
                new EvaluationInfo(application.interviewEvaluation().status(),
                    application.interviewEvaluation().score()),
                new FinalResultInfo(application.finalResult().status(), application.finalResult().selectedPart())
            );
        }
    }

    public record ApplicantInfo(String nickname, String name) {
    }

    public record SchoolInfo(Long schoolId, String name) {
    }

    public record AppliedPartInfo(int priority, String key, String label) {
    }

    public record EvaluationInfo(String status, Integer score) {
    }

    public record FinalResultInfo(String status, String selectedPart) {
    }
}
