package com.umc.product.recruitment.application.port.in.query.dto;

import java.util.List;

public record ApplicationListForAdminInfo(
        Filters filters,
        long applicantCount,
        PaginationInfo pagination,
        List<ApplicationForAdminInfo> applications
) {
    public record Filters(
            Long chapterId,
            Long schoolId,
            String part,
            String keyword
    ) {
    }

    public record PaginationInfo(
            int page,
            int size,
            int totalPages,
            long totalElements
    ) {
    }

    public record ApplicationForAdminInfo(
            Long applicationId,
            Applicant applicant,
            School school,
            List<AppliedPart> appliedParts,
            Evaluation documentEvaluation,
            Evaluation interviewEvaluation,
            FinalResult finalResult
    ) {
    }

    public record Applicant(
            String nickname,
            String name
    ) {
    }

    public record School(
            Long schoolId,
            String name
    ) {
    }

    public record AppliedPart(
            int priority,
            String key,
            String label
    ) {
    }

    public record Evaluation(
            String status, // WAITING | FAILED | SCORED
            Integer score  // WAITING이면 null
    ) {
    }

    public record FinalResult(
            String status,      // WAITING | PASSED | FAILED
            String selectedPart // PASSED일 때만 존재, 아니면 null
    ) {
    }
}