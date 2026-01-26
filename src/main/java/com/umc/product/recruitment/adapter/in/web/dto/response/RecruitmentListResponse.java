package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RecruitmentListResponse(
        List<RecruitmentSummaryResponse> recruitments
) {
    public record RecruitmentSummaryResponse(
            String schoolName,
            String gisu,
            Long recruitmentId,
            String recruitmentName,
            LocalDate startDate,
            LocalDate endDate,
            int applicantCount,
            String phase,
            String phaseLabel,   // 배지용 "진행 중/예정/종료"
            boolean editable,
            Instant updatedAt
    ) {
    }

    public static RecruitmentListResponse from(RecruitmentListInfo info) {
        return new RecruitmentListResponse(
                info.recruitments().stream()
                        .map(r -> new RecruitmentSummaryResponse(
                                r.schoolName(),
                                r.gisu(),
                                r.recruitmentId(),
                                r.recruitmentName(),
                                r.startDate(),
                                r.endDate(),
                                r.applicantCount(),
                                r.phase().name(),                         // phase
                                r.phase().name(), // 추후 phases -> 진행중/예정/종료로 매핑하는 매퍼 구현 예정
                                r.editable(),
                                r.updatedAt()
                        ))
                        .toList()
        );
    }
}
