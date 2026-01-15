package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;

public interface GetRecruitmentListUseCase {
    RecruitmentListInfo getList(GetRecruitmentListQuery query);
}
