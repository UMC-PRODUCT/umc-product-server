package com.umc.product.project.application.service.command;

import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.AnswerCommand;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateDraftFormResponseCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectApplicationCommandService implements
    CreateDraftProjectApplicationUseCase,
    UpdateProjectApplicationDraftUseCase,
    SubmitProjectApplicationUseCase {

    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final ManageFormResponseUseCase manageFormResponseUseCase;

    @Override
    public ProjectApplicationInfo create(CreateDraftProjectApplicationCommand command) {
        ProjectApplicationForm form = loadProjectApplicationFormPort.findByProjectId(command.projectId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND));

        // 멱등 처리 — 기존 DRAFT 있으면 그대로 반환
        return loadProjectApplicationPort
            .findByProjectIdAndApplicantMemberIdAndStatus(
                command.projectId(), command.applicantMemberId(), ProjectApplicationStatus.DRAFT)
            .map(existing -> ProjectApplicationInfo.of(existing.getId(), existing.getStatus()))
            .orElseGet(() -> createNew(form, command));
    }

    private ProjectApplicationInfo createNew(ProjectApplicationForm form, CreateDraftProjectApplicationCommand command) {
        Long formResponseId = manageFormResponseUseCase.createDraft(
            CreateDraftFormResponseCommand.builder()
                .formId(form.getFormId())
                .respondentMemberId(command.applicantMemberId())
                .build()
        );

        // TODO: 매칭라운드 PR 머지 후 appliedMatchingRound 연결
        ProjectApplication saved = saveProjectApplicationPort.save(
            ProjectApplication.create(form, formResponseId, command.applicantMemberId(), null)
        );

        return ProjectApplicationInfo.of(saved.getId(), saved.getStatus());
    }

    @Override
    public ProjectApplicationInfo update(UpdateProjectApplicationDraftCommand command) {
        ProjectApplication application = getDraftOrThrow(command.projectId(), command.requesterMemberId());

        manageFormResponseUseCase.updateDraft(
            UpdateDraftFormResponseCommand.builder()
                .formResponseId(application.getFormResponseId())
                .requesterMemberId(command.requesterMemberId())
                .answers(toAnswerCommands(command.answers()))
                .build()
        );

        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    @Override
    public ProjectApplicationInfo submit(SubmitProjectApplicationCommand command) {
        ProjectApplication application = getDraftOrThrow(command.projectId(), command.requesterMemberId());

        // Survey submitDraft — 필수 답변 누락 검증 포함
        manageFormResponseUseCase.submitDraft(
            SubmitDraftFormResponseCommand.builder()
                .formResponseId(application.getFormResponseId())
                .requesterMemberId(command.requesterMemberId())
                .build()
        );

        application.submit();
        saveProjectApplicationPort.save(application);

        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    private ProjectApplication getDraftOrThrow(Long projectId, Long memberId) {
        return loadProjectApplicationPort
            .findByProjectIdAndApplicantMemberIdAndStatus(projectId, memberId, ProjectApplicationStatus.DRAFT)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));
    }

    private List<AnswerCommand> toAnswerCommands(List<UpdateProjectApplicationDraftCommand.AnswerEntry> entries) {
        return entries.stream()
            .map(e -> AnswerCommand.builder()
                .questionId(e.questionId())
                .textValue(e.textValue())
                .selectedOptionIds(e.selectedOptionIds() == null ? List.of() : e.selectedOptionIds())
                .fileIds(e.fileIds() == null ? List.of() : e.fileIds())
                .build())
            .toList();
    }
}
