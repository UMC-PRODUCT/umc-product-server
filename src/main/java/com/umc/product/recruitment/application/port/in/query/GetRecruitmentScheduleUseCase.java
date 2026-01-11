package com.umc.product.recruitment.application.port.in.query;

public interface GetRecruitmentScheduleUseCase {
    RecruitmentScheduleInfo get(GetRecruitmentScheduleQuery query);
}
