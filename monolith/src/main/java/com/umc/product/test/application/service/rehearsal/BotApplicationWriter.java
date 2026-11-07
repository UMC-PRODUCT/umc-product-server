package com.umc.product.test.application.service.rehearsal;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand.AnswerEntry;
import com.umc.product.project.application.port.in.query.GetProjectApplicationFormUseCase;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.test.domain.rehearsal.ApplyPlan.PlannedApplication;

import lombok.RequiredArgsConstructor;

/**
 * 봇 지원자 한 명. 초안 생성, 답변, 제출을 한 트랜잭션으로 묶어 중간에 실패해도 초안이 남지 않는다.
 * 같은 차수에 초안이 이미 있으면 새로 만들지 않고(운영 규칙상 PROJECT-0210) 그 초안을 이어 제출한다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class BotApplicationWriter {

    private final CreateDraftProjectApplicationUseCase createDraftProjectApplicationUseCase;
    private final GetProjectApplicationFormUseCase getProjectApplicationFormUseCase;
    private final UpdateProjectApplicationDraftUseCase updateProjectApplicationDraftUseCase;
    private final SubmitProjectApplicationUseCase submitProjectApplicationUseCase;

    @Transactional
    public void write(PlannedApplication planned, Long roundId) {
        Long applicationId = planned.draftApplicationId() != null ? planned.draftApplicationId()
            : createDraftProjectApplicationUseCase.create(CreateDraftProjectApplicationCommand.builder()
                .projectId(planned.projectId()).applicantMemberId(planned.memberId()).matchingRoundId(roundId)
                .build()).applicationId();
        List<AnswerEntry> answers = getProjectApplicationFormUseCase
            .findByProjectId(planned.projectId(), planned.memberId())
            .map(BotAnswers::of)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND));
        updateProjectApplicationDraftUseCase.update(UpdateProjectApplicationDraftCommand.builder()
            .projectId(planned.projectId()).applicationId(applicationId).requesterMemberId(planned.memberId())
            .answers(answers).build());
        submitProjectApplicationUseCase.submit(SubmitProjectApplicationCommand.builder()
            .projectId(planned.projectId()).applicationId(applicationId).requesterMemberId(planned.memberId())
            .build());
    }
}
