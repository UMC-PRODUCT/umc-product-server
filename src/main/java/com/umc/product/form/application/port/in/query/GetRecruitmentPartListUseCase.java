package com.umc.product.form.application.port.in.query;

public interface GetRecruitmentPartListUseCase {
    RecruitmentPartListInfo getByRecruitmentId(GetRecruitmentPartListQuery query);
}
