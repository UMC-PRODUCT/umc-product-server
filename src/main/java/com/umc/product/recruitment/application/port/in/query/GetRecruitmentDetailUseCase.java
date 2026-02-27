package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;

public interface GetRecruitmentDetailUseCase {
    RecruitmentDraftInfo get(GetRecruitmentDetailQuery query);
}
