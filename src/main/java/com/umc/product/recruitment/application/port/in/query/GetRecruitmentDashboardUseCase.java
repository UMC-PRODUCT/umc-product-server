package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;

public interface GetRecruitmentDashboardUseCase {
    RecruitmentDashboardInfo get(Long recruitmentId, Long memberId);
}
