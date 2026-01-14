package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;

public interface GetActiveRecruitmentUseCase {
    ActiveRecruitmentInfo get(GetActiveRecruitmentQuery query);
}
