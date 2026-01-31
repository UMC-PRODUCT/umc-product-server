package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.domain.enums.RecruitmentPhase;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RecruitmentListResponse(List<RecruitmentSummaryResponse> recruitments) {
    public record RecruitmentSummaryResponse(Long recruitmentId, String recruitmentName, LocalDate startDate,
                                             LocalDate endDate, int applicantCount, String status, // DRAFT / PUBLISHED
                                             String phase, String listBadge,   // 배지용 "진행 중/예정/종료"
                                             boolean editable, Instant updatedAt) {
    }

    public static RecruitmentListResponse from(RecruitmentListInfo info) {
        return new RecruitmentListResponse(info.recruitments().stream()
                .map(r -> new RecruitmentSummaryResponse(r.recruitmentId(), r.recruitmentName(), r.startDate(),
                        r.endDate(), r.applicantCount(), r.status().name(),
                        r.status().isDraft() ? null : (r.phase() == null ? null : r.phase().name()),
                        toListBadge(r.status(), r.phase()), r.editable(), r.updatedAt())).toList());
    }

    private static String toListBadge(RecruitmentStatus status, RecruitmentPhase phase) {
        if (status == RecruitmentStatus.DRAFT) {
            return "임시저장";
        }
        if (phase == null) {
            return "진행 중";
        }

        return switch (phase) {
            case BEFORE_APPLY -> "모집 예정";
            case FINAL_RESULT_PUBLISHED, CLOSED -> "모집 종료";
            default -> "진행 중";
        };
    }
}
