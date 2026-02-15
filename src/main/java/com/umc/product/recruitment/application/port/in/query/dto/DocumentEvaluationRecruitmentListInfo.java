package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.time.Instant;
import java.util.List;

public record DocumentEvaluationRecruitmentListInfo(
    List<DocumentEvaluationRecruitmentInfo> evaluatingRecruitments, // 서류 평가 중
    List<DocumentEvaluationRecruitmentInfo> completeRecruitments    // 서류 평가 완료
) {
    public record DocumentEvaluationRecruitmentInfo(
        Long recruitmentId,
        Long rootRecruitmentId,
        String title,
        Instant recruitmentStartAt, // 모집 시작일
        Instant recruitmentEndAt,   // 최종 발표일
        Long totalApplicantCount,
        List<PartKey> openParts
    ) {
    }
}
