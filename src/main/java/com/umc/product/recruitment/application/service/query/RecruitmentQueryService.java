package com.umc.product.recruitment.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDashboardUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentPartListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentPartListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentScheduleQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
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
        GetRecruitmentDetailUseCase,
        GetRecruitmentPartListUseCase {

    private final LoadRecruitmentPort loadRecruitmentPort;

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
        loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // TODO: 권한 검증 필요 (memberId 기반)
        // if (!isAdminOrAuthor(query.memberId())) {
        //     throw new BusinessException(Domain.RECRUITMENT, ErrorCode.FORBIDDEN);
        // }

        return loadRecruitmentPort.findDraftInfoById(query.recruitmentId());
    }

    @Override
    public RecruitmentPartListInfo get(GetRecruitmentPartListQuery query) {
        return null;
    }
}
