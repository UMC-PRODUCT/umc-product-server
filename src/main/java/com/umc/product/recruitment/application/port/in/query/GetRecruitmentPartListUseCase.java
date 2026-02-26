package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentPartListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;

public interface GetRecruitmentPartListUseCase {
    RecruitmentPartListInfo get(GetRecruitmentPartListQuery query);
}
