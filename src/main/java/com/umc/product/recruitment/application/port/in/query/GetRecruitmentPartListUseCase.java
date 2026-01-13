package com.umc.product.recruitment.application.port.in.query;

public interface GetRecruitmentPartListUseCase {
    RecruitmentPartListInfo getByRecruitmentId(GetRecruitmentPartListQuery query);
}
