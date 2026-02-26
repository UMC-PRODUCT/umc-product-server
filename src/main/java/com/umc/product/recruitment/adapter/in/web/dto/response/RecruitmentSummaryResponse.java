package com.umc.product.recruitment.adapter.in.web.dto.response;

import java.time.LocalDate;

public record RecruitmentSummaryResponse(
    String schoolName,
    String gisu,
    Long recruitmentId,
    String recruitmentName,
    LocalDate startDate,
    LocalDate endDate,
    int applicantCount,
    boolean editable
) {
}
