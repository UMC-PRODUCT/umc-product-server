package com.umc.product.recruitment.application.port.in.query;

public interface GetRecruitmentNoticeUseCase {
    RecruitmentNoticeInfo get(GetRecruitmentNoticeQuery query);
}
