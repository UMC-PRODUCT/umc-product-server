package com.umc.product.recruitment.application.port.in.query;

public interface GetRecruitmentListUseCase {
    RecruitmentListInfo getList(GetRecruitmentListQuery query);
}
