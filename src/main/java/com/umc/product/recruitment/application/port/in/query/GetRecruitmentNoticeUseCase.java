package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;

public interface GetRecruitmentNoticeUseCase {
    RecruitmentNoticeInfo get(GetRecruitmentNoticeQuery query);
}
