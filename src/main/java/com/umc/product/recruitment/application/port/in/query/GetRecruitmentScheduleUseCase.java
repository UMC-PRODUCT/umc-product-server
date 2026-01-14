package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentScheduleQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;

public interface GetRecruitmentScheduleUseCase {
    RecruitmentScheduleInfo get(GetRecruitmentScheduleQuery query);
}
