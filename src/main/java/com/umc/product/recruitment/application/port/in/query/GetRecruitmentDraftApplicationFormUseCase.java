package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDraftApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;

public interface GetRecruitmentDraftApplicationFormUseCase {
    RecruitmentApplicationFormInfo get(GetRecruitmentDraftApplicationFormQuery query);
}
