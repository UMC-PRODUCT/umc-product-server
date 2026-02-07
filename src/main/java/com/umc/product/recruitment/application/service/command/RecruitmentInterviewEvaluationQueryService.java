package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.query.GetInterviewAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationViewUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewOptionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetLiveQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentInterviewEvaluationQueryService implements GetInterviewEvaluationViewUseCase,
    GetMyInterviewEvaluationUseCase,
    GetInterviewEvaluationSummaryUseCase,
    GetLiveQuestionsUseCase,
    GetInterviewAssignmentsUseCase,
    GetInterviewOptionsUseCase {
    @Override
    public GetInterviewEvaluationViewInfo get(GetInterviewEvaluationViewQuery query) {
        // InterviewQuetsionSheet에서 사용자의 1, 2지망에 해당하는 파트의 사전 질문을 조회해와야 합니다.
        // 사용자의 1, 2지망은 ApplicationPartPreference 엔티티에서 조회할 수 있습니다.
        return null;
    }

    @Override
    public GetMyInterviewEvaluationInfo get(GetMyInterviewEvaluationQuery query) {
        return null;
    }

    @Override
    public GetInterviewEvaluationsInfo get(GetInterviewEvaluationSummaryQuery query) {
        return null;
    }

    @Override
    public GetLiveQuestionsInfo get(GetLiveQuestionsQuery query) {
        return null;
    }

    @Override
    public GetInterviewAssignmentsInfo get(GetInterviewAssignmentsQuery query) {
        return null;
    }

    @Override
    public GetInterviewOptionsInfo get(GetInterviewOptionsQuery query) {
        return null;
    }
}
