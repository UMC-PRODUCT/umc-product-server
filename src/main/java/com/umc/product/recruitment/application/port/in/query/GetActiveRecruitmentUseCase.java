package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;

public interface GetActiveRecruitmentUseCase {
    ActiveRecruitmentInfo getActiveRecruitment(Long memberId);
}
