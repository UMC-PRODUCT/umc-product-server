package com.umc.product.project.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.AnswerCommand;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateDraftFormResponseCommand;
import java.time.Instant;
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
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final ManageFormResponseUseCase manageFormResponseUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public ProjectApplicationInfo create(CreateDraftProjectApplicationCommand command) {
        ProjectApplicationForm form = loadProjectApplicationFormPort.findByProjectId(command.projectId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND));

        Project project = form.getProject();

        // 1. 챌린저 정보 조회 (현재 기수 챌린저 신분 + 파트 확인)
        ChallengerInfo challenger = getChallengerUseCase.getByMemberIdAndGisuId(
            command.applicantMemberId(), project.getGisuId()
        );

        // 2. 파트 체크 - 이 프로젝트가 내 파트를 모집 중인지
        if (!loadProjectPartQuotaPort.existsByProjectIdAndPart(command.projectId(), challenger.part())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }

        // 3. 기수 팀원 여부 체크 — 이미 이 기수에 소속된 팀이 있으면 불가
        if (loadProjectMemberPort.existsByGisuAndMember(project.getGisuId(), command.applicantMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM);
        }

        // 4. FE가 지정한 매칭 차수 조회 + 검증
        ProjectMatchingRound round = loadProjectMatchingRoundPort.getById(command.matchingRoundId());

        if (!round.isOpenAt(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ROUND_NOT_OPEN);
        }

        MatchingType expectedType = challenger.part() == ChallengerPart.DESIGN
            ? MatchingType.PLAN_DESIGN
            : MatchingType.PLAN_DEVELOPER;
        if (round.getType() != expectedType) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ROUND_TYPE_MISMATCH);
        }

        // 5. 멱등 처리 - 현재 오픈된 차수 기준으로 기존 DRAFT 조회
        return loadProjectApplicationPort
            .findByProjectIdAndApplicantMemberIdAndRoundIdAndStatus(
                command.projectId(),
                command.applicantMemberId(),
                round.getId(),
                ProjectApplicationStatus.DRAFT
            ).map(existing -> ProjectApplicationInfo.of(existing.getId(), existing.getStatus()))
            .orElseGet(() -> createNew(form, command.applicantMemberId(), round));
    }

    private ProjectApplicationInfo createNew(
        ProjectApplicationForm form, Long applicantMemberId, ProjectMatchingRound round
    ) {
        Long formResponseId = manageFormResponseUseCase.createDraft(
            CreateDraftFormResponseCommand.builder()
                .formId(form.getFormId())
                .respondentMemberId(applicantMemberId)
                .build()
        );

        ProjectApplication saved = saveProjectApplicationPort.save(
            ProjectApplication.create(form, formResponseId, applicantMemberId, round)
        );

        return ProjectApplicationInfo.of(saved.getId(), saved.getStatus());
    }

    @Override
    public ProjectApplicationInfo update(UpdateProjectApplicationDraftCommand command) {
        ProjectApplication application = loadProjectApplicationPort
            .getDraftByProjectAndMember(command.projectId(), command.requesterMemberId());

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
        ProjectApplication application = loadProjectApplicationPort
            .getDraftByProjectAndMember(command.projectId(), command.requesterMemberId());

        // 차수 마감 여부 체크 - 제출 시점에 차수가 닫혀있으면 불가
        if (application.getAppliedMatchingRound() != null
            && !application.getAppliedMatchingRound().isOpenAt(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ROUND_NOT_OPEN);
        }

        // 동일 차수 중복 제출 체크
        if (application.getAppliedMatchingRound() != null
            && loadProjectApplicationPort.existsByRoundAndApplicantAndStatus(
                application.getAppliedMatchingRound().getId(),
                command.requesterMemberId(),
                ProjectApplicationStatus.SUBMITTED)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_DUPLICATE_SUBMISSION);
        }

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
