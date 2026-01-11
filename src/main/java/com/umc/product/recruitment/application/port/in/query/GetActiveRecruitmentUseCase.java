package com.umc.product.recruitment.application.port.in.query;

public interface GetActiveRecruitmentUseCase {
    ActiveRecruitmentInfo get(GetActiveRecruitmentQuery query);
}
