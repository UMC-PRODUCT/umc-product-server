package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;

public interface GetRecruitmentApplicationFormUseCase {
    RecruitmentApplicationFormInfo get(GetRecruitmentApplicationFormQuery query);
}
