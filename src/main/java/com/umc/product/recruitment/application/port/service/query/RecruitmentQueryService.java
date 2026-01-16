package com.umc.product.recruitment.application.port.service.query;

import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements GetActiveRecruitmentUseCase, GetRecruitmentNoticeUseCase,
        GetRecruitmentApplicationFormUseCase {

    @Override
    public ActiveRecruitmentInfo get(GetActiveRecruitmentQuery query) {
        return null;
    }

    @Override
    public RecruitmentNoticeInfo get(GetRecruitmentNoticeQuery query) {

        return null;
    }

    @Override
    public RecruitmentApplicationFormInfo get(GetRecruitmentApplicationFormQuery query) {

        return null;
    }
}
