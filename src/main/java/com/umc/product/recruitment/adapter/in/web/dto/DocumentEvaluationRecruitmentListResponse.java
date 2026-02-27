package com.umc.product.recruitment.adapter.in.web.dto;

import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record DocumentEvaluationRecruitmentListResponse(
    List<DocumentEvaluationRecruitmentResponse> evaluatingRecruitments, // 서류 평가 중
    List<DocumentEvaluationRecruitmentResponse> completeRecruitments    // 서류 평가 완료
) {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public static DocumentEvaluationRecruitmentListResponse from(DocumentEvaluationRecruitmentListInfo info) {
        List<DocumentEvaluationRecruitmentResponse> evaluatingItems = info.evaluatingRecruitments().stream()
            .map(DocumentEvaluationRecruitmentResponse::from)
            .toList();

        List<DocumentEvaluationRecruitmentResponse> completeItems = info.completeRecruitments().stream()
            .map(DocumentEvaluationRecruitmentResponse::from)
            .toList();

        return new DocumentEvaluationRecruitmentListResponse(evaluatingItems, completeItems);
    }

    public record DocumentEvaluationRecruitmentResponse(
        Long recruitmentId,
        Long rootRecruitmentId,
        String title,
        LocalDate recruitmentStartDate, // 전체 모집 시작일
        LocalDate recruitmentEndDate,   // 최종 결과 발표일
        Long totalApplicantCount,
        List<PartResponse> openParts
    ) {
        public static DocumentEvaluationRecruitmentResponse from(
            DocumentEvaluationRecruitmentListInfo.DocumentEvaluationRecruitmentInfo i
        ) {
            LocalDate startDate = (i.recruitmentStartAt() == null)
                ? null
                : i.recruitmentStartAt().atZone(ZONE_ID).toLocalDate();

            LocalDate endDate = (i.recruitmentEndAt() == null)
                ? null
                : i.recruitmentEndAt().atZone(ZONE_ID).toLocalDate();

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
