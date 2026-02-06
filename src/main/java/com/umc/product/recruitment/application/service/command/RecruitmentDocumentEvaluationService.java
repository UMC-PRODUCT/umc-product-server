package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.UpdateMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyDocumentEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.UpdateDocumentStatusUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo.MyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadEvaluationPort;
import com.umc.product.recruitment.application.port.out.SaveEvaluationPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.EvaluationStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentDocumentEvaluationService implements UpdateMyDocumentEvaluationUseCase,
    UpdateDocumentStatusUseCase {

    private final LoadApplicationListPort loadApplicationListPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadEvaluationPort loadEvaluationPort;
    private final SaveEvaluationPort saveEvaluationPort;

    @Override
    public GetMyDocumentEvaluationInfo update(UpdateMyDocumentEvaluationCommand command) {
        // todo: 평가 기간, 운영진 권한 검증 필요

        Long recruitmentId = command.recruitmentId();
        Long applicationId = command.applicationId();
        Long evaluatorMemberId = command.evaluatorMemberId();

        // 1. 해당 지원서가 이 모집에 속하는지 검증
        if (!loadApplicationListPort.isApplicationBelongsToRecruitment(applicationId, recruitmentId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. 저장할 상태 결정 (SUBMIT → SUBMITTED, DRAFT_SAVE → DRAFT)
        EvaluationStatus targetStatus = command.isSubmit()
            ? EvaluationStatus.SUBMITTED
            : EvaluationStatus.DRAFT;

        // 3. 기존 평가 조회 (있으면 업데이트, 없으면 생성)
        Evaluation evaluation = loadEvaluationPort
            .findByApplicationIdAndEvaluatorUserIdAndStage(applicationId, evaluatorMemberId, EvaluationStage.DOCUMENT)
            .map(existing -> {
                existing.update(command.score(), command.comments(), targetStatus);
                return existing;
            })
            .orElseGet(() -> {
                Application application = loadApplicationPort.findById(applicationId)
                    .orElseThrow(() -> new RecruitmentDomainException(
                        RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT));

                return Evaluation.createDocumentEvaluation(
                    application,
                    evaluatorMemberId,
                    command.score(),
                    command.comments(),
                    targetStatus
                );
            });

        // 4. 저장
        Evaluation savedEvaluation = saveEvaluationPort.save(evaluation);

        // 5. 응답 반환
        return new GetMyDocumentEvaluationInfo(
            new MyDocumentEvaluationInfo(
                applicationId,
                savedEvaluation.getId(),
                savedEvaluation.getScore(),
                savedEvaluation.getComments(),
                savedEvaluation.getStatus() == EvaluationStatus.SUBMITTED,
                savedEvaluation.getUpdatedAt()
            )
        );
    }

    @Override
    public UpdateDocumentStatusInfo update(UpdateDocumentStatusCommand command) {
        // todo: 평가 기간, 운영진 권한 검증 필요
        return null;
    }
}
