package com.umc.product.recruitment.adapter.in.web.dto;

import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record DocumentEvaluationRecruitmentListResponse(
    List<DocumentEvaluationRecruitmentResponse> recruitments
) {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public static DocumentEvaluationRecruitmentListResponse from(DocumentEvaluationRecruitmentListInfo info) {
        List<DocumentEvaluationRecruitmentResponse> items = info.recruitments().stream()
            .map(DocumentEvaluationRecruitmentResponse::from)
            .toList();

        return new DocumentEvaluationRecruitmentListResponse(items);
    }

    public record DocumentEvaluationRecruitmentResponse(
        Long recruitmentId,
        Long rootRecruitmentId,
        String title,
        LocalDate docReviewStartDate,
        LocalDate docReviewEndDate,
        Long totalApplicantCount,
        List<PartResponse> openParts
    ) {
        public static DocumentEvaluationRecruitmentResponse from(
            DocumentEvaluationRecruitmentListInfo.DocumentEvaluationRecruitmentInfo i
        ) {
            LocalDate startDate = (i.docReviewStartAt() == null)
                ? null
                : i.docReviewStartAt().atZone(ZONE_ID).toLocalDate();

            LocalDate endDate = (i.docReviewEndAt() == null)
                ? null
                : i.docReviewEndAt().atZone(ZONE_ID).toLocalDate();

            List<PartResponse> parts = (i.openParts() == null)
                ? List.of()
                : i.openParts().stream()
                    .map(PartResponse::from)
                    .toList();

            return new DocumentEvaluationRecruitmentResponse(
                i.recruitmentId(),
                i.rootRecruitmentId(),
                i.title(),
                startDate,
                endDate,
                i.totalApplicantCount(),
                parts
            );
        }

        public record PartResponse(
            String key,
            String label
        ) {
            public static PartResponse from(PartKey partKey) {
                return new PartResponse(partKey.name(), partKey.getLabel());
            }
        }
    }
}
