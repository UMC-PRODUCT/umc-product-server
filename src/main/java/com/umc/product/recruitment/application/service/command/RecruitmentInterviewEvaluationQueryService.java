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
import com.umc.product.recruitment.application.port.out.LoadEvaluationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewLiveQuestionPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final LoadEvaluationPort loadEvaluationPort;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public GetInterviewEvaluationViewInfo get(GetInterviewEvaluationViewQuery query) {
        // InterviewQuetsionSheet에서 사용자의 1, 2지망에 해당하는 파트의 사전 질문을 조회해와야 합니다.
        // 사용자의 1, 2지망은 ApplicationPartPreference 엔티티에서 조회할 수 있습니다.
        return null;
    }

    @Override
    public GetMyInterviewEvaluationInfo get(GetMyInterviewEvaluationQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(query.assignmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(query.recruitmentId())) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. Application 가져오기
        Application application = assignment.getApplication();

        // 3. 내 면접 평가 조회
        return loadEvaluationPort.findByApplicationIdAndEvaluatorUserIdAndStage(
            application.getId(),
            query.memberId(),
            EvaluationStage.INTERVIEW
        ).map(evaluation -> new GetMyInterviewEvaluationInfo(
            new GetMyInterviewEvaluationInfo.MyInterviewEvaluationInfo(
                evaluation.getId(),
                evaluation.getScore(),
                evaluation.getComments(),
                evaluation.getUpdatedAt()
            )
        )).orElse(new GetMyInterviewEvaluationInfo(null));
    }

    @Override
    public GetInterviewEvaluationsInfo get(GetInterviewEvaluationSummaryQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(query.assignmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(query.recruitmentId())) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. Application 가져오기
        Application application = assignment.getApplication();

        // 3. Application에 해당하는 Evaluation 리스트 가져오기
        List<Evaluation> evaluations = loadEvaluationPort.findByApplicationIdAndStage(
            application.getId(),
            EvaluationStage.INTERVIEW
        );

        if (evaluations.isEmpty()) {
            return new GetInterviewEvaluationsInfo(null, List.of());
        }

        // 4. 평가자 ID 목록 추출 & 일괄 조회
        Set<Long> evaluatorMemberIds = evaluations.stream()
            .map(Evaluation::getEvaluatorUserId)
            .collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> profileMap = getMemberUseCase.getProfiles(evaluatorMemberIds);

        // 5. 평균 점수 계산
        Double avgScore = evaluations.stream()
            .map(Evaluation::getScore)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        // 6. 평가 리스트 생성
        List<GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo> items = evaluations.stream()
            .map(evaluation -> {
                MemberProfileInfo evaluator = profileMap.get(evaluation.getEvaluatorUserId());
                return new GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo(
                    new GetInterviewEvaluationsInfo.Evaluator(
                        evaluator.id(),
                        evaluator.nickname(),
                        evaluator.name()
                    ),
                    evaluation.getScore(),
                    evaluation.getComments()
                );
            })
            .toList();

        return new GetInterviewEvaluationsInfo(avgScore, items);
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
