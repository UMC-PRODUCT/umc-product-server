package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.query.GetApplicationDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationEvaluationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentDocumentEvaluationQueryService implements GetApplicationDetailUseCase,
        GetApplicationListUseCase,
        GetApplicationEvaluationListUseCase,
        GetMyDocumentEvaluationUseCase {

    @Override
    public ApplicationDetailInfo get(GetApplicationDetailQuery query) {
        // todo: 운영진 권한 검증 필요
        return null;
    }

    @Override
    public ApplicationListInfo get(GetApplicationListQuery query) {
        // todo: 운영진 권한 검증 필요
        return null;
    }

    @Override
    public ApplicationEvaluationListInfo get(GetApplicationEvaluationListQuery query) {
        // todo: 운영진 권한 검증 필요
        return null;
    }

    @Override
    public GetMyDocumentEvaluationInfo get(GetMyDocumentEvaluationQuery query) {
        // todo: 본인 검증 필요
        return null;
    }
}
