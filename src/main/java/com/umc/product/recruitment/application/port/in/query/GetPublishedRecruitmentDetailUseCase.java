package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetPublishedRecruitmentDetailQuery;

public interface GetPublishedRecruitmentDetailUseCase {
    RecruitmentPublishedInfo get(GetPublishedRecruitmentDetailQuery query);
}
