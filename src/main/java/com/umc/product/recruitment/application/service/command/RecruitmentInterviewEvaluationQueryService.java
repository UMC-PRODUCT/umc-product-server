package com.umc.product.recruitment.application.service.command;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo.CreatedBy;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo.LiveQuestionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationQuery;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewLiveQuestionPort;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentInterviewEvaluationQueryService implements GetInterviewEvaluationViewUseCase,
    GetMyInterviewEvaluationUseCase,
    GetInterviewEvaluationSummaryUseCase,
    GetLiveQuestionsUseCase,
    GetInterviewAssignmentsUseCase,
    GetInterviewOptionsUseCase {

    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final LoadInterviewLiveQuestionPort loadInterviewLiveQuestionPort;
    private final GetMemberUseCase getMemberUseCase;

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
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(query.assignmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(query.recruitmentId())) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. application의 live questions 조회
        Long applicationId = assignment.getApplication().getId();
        List<InterviewLiveQuestion> questions = loadInterviewLiveQuestionPort.findByApplicationIdOrderByIdAsc(
            applicationId);

        if (questions.isEmpty()) {
            return new GetLiveQuestionsInfo(List.of());
        }

        // 3. 작성자 ID 목록 추출 & 일괄 조회
        Set<Long> authorMemberIds = questions.stream()
            .map(InterviewLiveQuestion::getAuthorMemberId)
            .collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> profileMap = getMemberUseCase.getProfiles(authorMemberIds);

        // 4. LiveQuestionInfo 리스트 생성
        List<LiveQuestionInfo> items = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            InterviewLiveQuestion question = questions.get(i);
            MemberProfileInfo author = profileMap.get(question.getAuthorMemberId());

            items.add(new LiveQuestionInfo(
                question.getId(),
                i + 1,  // orderNo
                question.getContent(),
                new CreatedBy(author.id(), author.nickname(), author.name()),
                question.getAuthorMemberId().equals(query.memberId())  // canEdit
            ));
        }

        return new GetLiveQuestionsInfo(items);
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
