package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record ApplicationListForAdminInfo(
    Filters filters,
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
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
    ) {
    }

    public record ApplicationForAdminInfo(
        Long applicationId,
        Applicant applicant,
        School school,
        List<AppliedPart> appliedParts,
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
        PartKey part
    ) {
    }

    public record Evaluation(
        String status, // WAITING | FAILED | SCORED
        Integer score  // WAITING이면 null
    ) {
    }

    public record FinalResult(
        String status,      // PASS | FAIL | WAIT
        PartKey selectedPart // PASS일 때만 존재, 아니면 null
    ) {
    }
}
