package com.umc.product.recruitment.application.service.command;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.recruitment.application.port.in.command.CreateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionResult.CreatedBy;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertMyInterviewEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.out.LoadEvaluationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewLiveQuestionPort;
import com.umc.product.recruitment.application.port.out.SaveEvaluationPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewLiveQuestionPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecruitmentInterviewEvaluationService implements UpsertMyInterviewEvaluationUseCase,
    CreateLiveQuestionUseCase,
    UpdateLiveQuestionUseCase,
    DeleteLiveQuestionUseCase {

    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final LoadInterviewLiveQuestionPort loadInterviewLiveQuestionPort;
    private final SaveInterviewLiveQuestionPort saveInterviewLiveQuestionPort;
    private final LoadEvaluationPort loadEvaluationPort;
    private final SaveEvaluationPort saveEvaluationPort;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public GetMyInterviewEvaluationInfo upsert(UpsertMyInterviewEvaluationCommand command) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(command.assignmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(command.recruitmentId())) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. Application 가져오기
        Application application = assignment.getApplication();

        // 3. 기존 평가 조회 (upsert 판별)
        Evaluation evaluation = loadEvaluationPort.findByApplicationIdAndEvaluatorUserIdAndStage(
            application.getId(),
            command.evaluatorMemberId(),
            EvaluationStage.INTERVIEW
        ).orElse(null);

        // 4. Upsert : 없으면 생성, 있으면 업데이트
        if (evaluation == null) {
            evaluation = Evaluation.createInterviewEvaluation(
                application,
                command.evaluatorMemberId(),
                command.score(),
                command.comments()
            );
        } else {
            evaluation.updateScoreAndComments(command.score(), command.comments());
        }

        // 5. 평가 저장
        Evaluation saved = saveEvaluationPort.save(evaluation);

        return new GetMyInterviewEvaluationInfo(
            new GetMyInterviewEvaluationInfo.MyInterviewEvaluationInfo(
                saved.getId(),
                saved.getScore(),
                saved.getComments(),
                saved.getUpdatedAt()
            )
        );
    }

    @Override
    public CreateLiveQuestionResult create(CreateLiveQuestionCommand command) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(command.assignmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(command.recruitmentId())) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. Application 가져오기 (assignment → application)
        Application application = assignment.getApplication();

        // 3. orderNo 계산 (해당 application의 live question 개수 + 1)
        int orderNo = loadInterviewLiveQuestionPort.countByApplicationId(application.getId()) + 1;

        // 4. InterviewLiveQuestion 생성 & 저장
        InterviewLiveQuestion liveQuestion = InterviewLiveQuestion.builder()
            .application(application)
            .authorMemberId(command.memberId())
            .content(command.text())
            .build();

        InterviewLiveQuestion saved = saveInterviewLiveQuestionPort.save(liveQuestion);

        // 5. 작성자 정보 조회
        MemberInfo memberInfo = getMemberUseCase.getById(command.memberId());

        return new CreateLiveQuestionResult(
            saved.getId(),
            orderNo,
            saved.getContent(),
            new CreatedBy(memberInfo.id(), memberInfo.nickname(), memberInfo.name()),
            true
        );
    }

    @Override
    public UpdateLiveQuestionResult update(UpdateLiveQuestionCommand command) {
        InterviewLiveQuestion question = validateAndGetQuestion(command.recruitmentId(), command.assignmentId(),
            command.liveQuestionId(), command.memberId());

        // 질문 내용 update
        question.changeContent(command.text());

        return new UpdateLiveQuestionResult(
            question.getId(),
            question.getContent()
        );
    }

    @Override
    public void delete(DeleteLiveQuestionCommand command) {
        InterviewLiveQuestion question = validateAndGetQuestion(command.recruitmentId(), command.assignmentId(),
            command.liveQuestionId(), command.memberId());

        // 질문 삭제
        saveInterviewLiveQuestionPort.deleteById(question.getId());
    }

    // 즉석 질문 수정, 삭제를 위한 검증 private method
    private InterviewLiveQuestion validateAndGetQuestion(Long recruitmentId, Long assignmentId, Long liveQuestionId,
                                                         Long memberId) {

        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(assignmentId)
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(recruitmentId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. 검증 : InterviewLiveQuestion 존재
        InterviewLiveQuestion question = loadInterviewLiveQuestionPort.findById(liveQuestionId)
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_LIVE_QUESTION_NOT_FOUND));

        // 3. 검증: 질문이 해당 assignment의 application에 속하는지
        if (!question.getApplication().getId().equals(assignment.getApplication().getId())) {
            throw new RecruitmentDomainException(
                RecruitmentErrorCode.INTERVIEW_LIVE_QUESTION_NOT_BELONGS_TO_ASSIGNMENT);
        }

        // 4. 검증 : 작성자와 요청자의 일치 여부
        if (!question.getAuthorMemberId().equals(memberId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_LIVE_QUESTION_NOT_EDITABLE);
        }

        return question;
    }

}
