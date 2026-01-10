package com.umc.product.survey.application.port.in.query;

public interface GetRecruitmentPartListUseCase {
    RecruitmentPartListInfo getByRecruitmentId(GetRecruitmentPartListQuery query);
}
