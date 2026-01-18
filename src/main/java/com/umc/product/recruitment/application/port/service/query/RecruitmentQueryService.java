package com.umc.product.recruitment.application.port.service.query;

import com.umc.product.recruitment.adapter.in.web.dto.response.GetRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDashboardUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentScheduleQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements GetActiveRecruitmentUseCase, GetRecruitmentNoticeUseCase,
        GetRecruitmentApplicationFormUseCase,
        GetRecruitmentFormResponseDetailUseCase,
        GetRecruitmentListUseCase,
        GetRecruitmentScheduleUseCase,
        GetRecruitmentDashboardUseCase,
        GetMyApplicationListUseCase,
        GetRecruitmentDetailUseCase {

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

    @Override
    public RecruitmentFormResponseDetailInfo get(GetRecruitmentFormResponseDetailQuery query) {
        return null;
    }

    @Override
    public RecruitmentListInfo getList(GetRecruitmentListQuery query) {
        return null;
    }

    @Override
    public RecruitmentScheduleInfo get(GetRecruitmentScheduleQuery query) {
        return null;
    }

    @Override
    public RecruitmentDashboardInfo get(Long recruitmentId) {
        return null;
    }

    @Override
    public MyApplicationListInfo get(GetMyApplicationListQuery query) {
        return null;
    }

    @Override
    public RecruitmentDraftInfo get(GetRecruitmentDetailQuery query) {
        return null;
    }
}
