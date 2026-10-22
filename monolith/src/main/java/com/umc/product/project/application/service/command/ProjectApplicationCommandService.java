package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.form.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.form.application.port.in.command.dto.AnswerCommand;
import com.umc.product.form.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.form.application.port.in.command.dto.SubmitDraftFormResponseCommand;
import com.umc.product.form.application.port.in.command.dto.UpdateDraftFormResponseCommand;
import com.umc.product.form.application.port.in.query.GetFormUseCase;
import com.umc.product.form.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.port.in.command.CancelProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.DecideApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.command.dto.CancelProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.service.policy.ProjectApplicationChapterPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectApplicationCommandService implements
    CreateDraftProjectApplicationUseCase,
    UpdateProjectApplicationDraftUseCase,
    SubmitProjectApplicationUseCase,
    DecideApplicationUseCase,
    CancelProjectApplicationUseCase {

    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final LoadProjectApplicationFormPolicyPort loadProjectApplicationFormPolicyPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final ManageFormResponseUseCase manageFormResponseUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ProjectApplicationChapterPolicy projectApplicationChapterPolicy;
    private final GetFormUseCase getFormUseCase;

    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.CREATE,
        targetType = "ProjectApplication",
        targetId = "#result.applicationId()",
        description = "'프로젝트 지원서 초안을 생성했습니다.'"
    )
    @Override
    public ProjectApplicationInfo create(CreateDraftProjectApplicationCommand command) {
        ProjectApplicationForm form = loadProjectApplicationFormPort.findByProjectId(command.projectId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND));

        Project project = form.getProject();

        // 0-a. 부모 프로젝트가 모집 단계인지 검증 (도메인 규칙)
        project.validateApplicable();

        // 0-b. 자기지원 차단 — PM 본인은 자기 프로젝트에 지원할 수 없다 (도메인 규칙: PO ↔ applicant 역할 충돌 방지)
        if (project.getProductOwnerMemberId().equals(command.applicantMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED);
        }

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

        // 4-a. 지부 일치 — 차수와 지원자 매칭 지부가 모두 프로젝트 지부여야 한다 (매칭 지부 이동 반영)
        projectApplicationChapterPolicy.validate(project, round, command.applicantMemberId());

        // 5. 동일 차수 기준 DRAFT 중복 체크 — 이미 있으면 409
        if (loadProjectApplicationPort.findByProjectIdAndApplicantMemberIdAndRoundIdAndStatus(
            command.projectId(),
            command.applicantMemberId(),
            round.getId(),
            ProjectApplicationStatus.DRAFT
        ).isPresent()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ALREADY_EXISTS);
        }

        return createNew(form, command.applicantMemberId(), round);
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
        ProjectApplication application = loadDraftApplication(
            command.projectId(), command.applicationId(), command.requesterMemberId()
        );
        VisibleQuestionScope questionScope = resolveVisibleQuestionScope(application);
        validateAnswerQuestionIdsAllowed(command.answers(), questionScope.allowedQuestionIds());

        manageFormResponseUseCase.updateDraft(
            UpdateDraftFormResponseCommand.builder()
                .formResponseId(application.getFormResponseId())
                .requesterMemberId(command.requesterMemberId())
                .answers(toAnswerCommands(command.answers()))
                .build()
        );

        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.SUBMIT,
        targetType = "ProjectApplication",
        targetId = "#result.applicationId()",
        description = "'프로젝트 지원서를 제출했습니다.'"
    )
    @Override
    public ProjectApplicationInfo submit(SubmitProjectApplicationCommand command) {
        ProjectApplication application = loadDraftApplication(
            command.projectId(), command.applicationId(), command.requesterMemberId()
        );

        // 차수 마감 여부 체크 - 제출 시점에 차수가 닫혀있으면 불가
        if (application.getAppliedMatchingRound() != null
            && !application.getAppliedMatchingRound().isOpenAt(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ROUND_NOT_OPEN);
        }

        // 지부 일치 재검증 — 임시저장 이후 매칭 지부가 바뀐 경우를 막는다
        projectApplicationChapterPolicy.validate(
            application.getApplicationForm().getProject(),
            application.getAppliedMatchingRound(),
            command.requesterMemberId()
        );

        // 동일 차수 중복 제출 체크
        if (application.getAppliedMatchingRound() != null
            && loadProjectApplicationPort.existsByRoundAndApplicantAndStatus(
                application.getAppliedMatchingRound().getId(),
                command.requesterMemberId(),
                ProjectApplicationStatus.SUBMITTED)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_DUPLICATE_SUBMISSION);
        }

        VisibleQuestionScope questionScope = resolveVisibleQuestionScope(application);

        // Form submitDraft — Project 가 계산한 노출 질문 scope 안에서 필수 답변 누락 검증 포함
        manageFormResponseUseCase.submitDraft(
            SubmitDraftFormResponseCommand.builder()
                .formResponseId(application.getFormResponseId())
                .requesterMemberId(command.requesterMemberId())
                .requiredQuestionIds(questionScope.requiredQuestionIds())
                .allowedQuestionIds(questionScope.allowedQuestionIds())
                .build()
        );

        application.submit();
        saveProjectApplicationPort.save(application);

        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    @Override
    public ProjectApplicationInfo cancel(CancelProjectApplicationCommand command) {
        ProjectApplication application = loadProjectApplicationPort.findById(command.applicationId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));

        // 매칭 차수 OPEN 검증 — 차수 종료 후에는 철회 불가
        if (!application.getAppliedMatchingRound().isOpenAt(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_ROUND_CLOSED);
        }

        // 상태 머신 가드는 도메인 책임 - DRAFT/SUBMITTED -> CANCELLED, 종결 상태는 도메인이 예외
        application.cancel(command.requesterMemberId(), command.reason());
        saveProjectApplicationPort.save(application);

        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    @Override
    public ProjectApplicationInfo decide(
        Long applicationId,
        ApplicationDecisionStatus targetStatus,
        String reason,
        Long decidedByMemberId
    ) {
        ProjectApplication application = loadProjectApplicationPort.findById(applicationId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));

        if (targetStatus == ApplicationDecisionStatus.APPROVED
            && application.getStatus() != ProjectApplicationStatus.APPROVED) {
            validateRemainingQuota(application);
        }
        if (targetStatus == ApplicationDecisionStatus.SUBMITTED) {
            validateNoOtherActiveApplication(application);
        }

        applyDecision(application, targetStatus, reason, decidedByMemberId);

        saveProjectApplicationPort.save(application);
        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
    }

    private void applyDecision(
        ProjectApplication application,
        ApplicationDecisionStatus targetStatus,
        String reason,
        Long decidedByMemberId
    ) {
        switch (targetStatus) {
            case APPROVED -> application.approve(decidedByMemberId, reason);
            case REJECTED -> application.reject(decidedByMemberId, reason);
            case SUBMITTED -> application.revertToSubmitted(decidedByMemberId, reason);
        }
    }

    /**
     * 대기(SUBMITTED)로 되돌리기 전, 같은 (프로젝트, 차수, 지원자)에 다른 활성(DRAFT/SUBMITTED) 지원서가 없는지 검증한다.
     * <p>
     * 활성 지원서는 partial unique index 로 (양식, 차수, 지원자)당 하나만 허용되므로, 다른 활성 지원서가 있는 채로 되돌리면
     * 저장 시점에 제약 위반이 난다. 그 전에 {@link ProjectErrorCode#PROJECT_APPLICATION_ALREADY_EXISTS} 로 막는다.
     */
    private void validateNoOtherActiveApplication(ProjectApplication application) {
        Long projectId = application.getApplicationForm().getProject().getId();
        Long roundId = application.getAppliedMatchingRound().getId();
        for (ProjectApplicationStatus activeStatus : List.of(
            ProjectApplicationStatus.DRAFT, ProjectApplicationStatus.SUBMITTED
        )) {
            boolean otherActiveExists = loadProjectApplicationPort
                .findByProjectIdAndApplicantMemberIdAndRoundIdAndStatus(
                    projectId, application.getApplicantMemberId(), roundId, activeStatus
                )
                .filter(other -> !other.getId().equals(application.getId()))
                .isPresent();
            if (otherActiveExists) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ALREADY_EXISTS);
            }
        }
    }

    /**
     * APPROVED 토글 시 잔여 자리 검증.
     * <p>
     * 잔여 자리 = 전체 TO − (이전 차수까지 ACTIVE 멤버) − (현재 차수에서 같은 (project, part) 의 APPROVED 카운트).
     * 이 값이 0 이하면 자리가 없으므로 예외를 던진다.
     */
    private void validateRemainingQuota(ProjectApplication application) {
        Project project = application.getApplicationForm().getProject();
        Long projectId = project.getId();
        Long gisuId = project.getGisuId();
        Long roundId = application.getAppliedMatchingRound().getId();

        ChallengerPart part = getChallengerUseCase
            .getByMemberIdAndGisuId(application.getApplicantMemberId(), gisuId)
            .part();

        int totalQuota = loadProjectPartQuotaPort.listByProjectId(projectId).stream()
            .filter(q -> q.getPart() == part)
            .findFirst()
            .map(q -> q.getQuota().intValue())
            .orElse(0);

        int activeMemberCount = loadProjectMemberPort
            .countByProjectIdGroupByPart(projectId)
            .getOrDefault(part, 0L)
            .intValue();

        int currentRoundApproved = countApprovedInSameRoundProjectPart(
            roundId, projectId, part, gisuId, application.getId()
        );

        if (activeMemberCount + currentRoundApproved + 1 > totalQuota) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_QUOTA_EXCEEDED);
        }
    }

    /**
     * 본 차수의 같은 (project, part) 안에서 APPROVED 인 application 수를 카운트한다 (현재 application 제외).
     * <p>
     * project / status 필터를 먼저 in-memory 로 좁혀 candidate 만 추린 뒤, candidate 들의 challenger 정보를
     * batch 한 번에 조회해 part 비교한다 (N+1 제거).
     */
    private int countApprovedInSameRoundProjectPart(
        Long roundId, Long projectId, ChallengerPart part, Long gisuId, Long excludingApplicationId
    ) {
        List<ProjectApplication> candidates = loadProjectApplicationPort.listByMatchingRoundId(roundId).stream()
            .filter(a -> !a.getId().equals(excludingApplicationId))
            .filter(a -> a.getStatus() == ProjectApplicationStatus.APPROVED)
            .filter(a -> a.getApplicationForm().getProject().getId().equals(projectId))
            .toList();
        if (candidates.isEmpty()) {
            return 0;
        }

        Set<Long> memberIds = candidates.stream()
            .map(ProjectApplication::getApplicantMemberId)
            .collect(Collectors.toSet());
        Map<Long, ChallengerInfo> challengerByMember =
            getChallengerUseCase.batchGetByMemberIdsAndGisuId(memberIds, gisuId);

        return (int) candidates.stream()
            .filter(a -> challengerByMember.get(a.getApplicantMemberId()).part() == part)
            .count();
    }

    private ProjectApplication loadDraftApplication(
        Long projectId,
        Long applicationId,
        Long requesterMemberId
    ) {
        if (applicationId == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        }

        ProjectApplication application = loadProjectApplicationPort.findByIdWithDetails(applicationId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));

        Long actualProjectId = application.getApplicationForm().getProject().getId();
        if (!Objects.equals(actualProjectId, projectId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        }
        if (!Objects.equals(application.getApplicantMemberId(), requesterMemberId) || !application.isDraft()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_DRAFT_APPLICATION_NOT_FOUND);
        }

        return application;
    }

    private VisibleQuestionScope resolveVisibleQuestionScope(ProjectApplication application) {
        ProjectApplicationForm applicationForm = application.getApplicationForm();
        Project project = applicationForm.getProject();
        ChallengerPart applicantPart = getChallengerUseCase
            .getByMemberIdAndGisuId(application.getApplicantMemberId(), project.getGisuId())
            .part();

        Map<Long, ProjectApplicationFormPolicy> policyBySectionId =
            loadProjectApplicationFormPolicyPort.listByApplicationFormId(applicationForm.getId()).stream()
                .collect(Collectors.toMap(
                    ProjectApplicationFormPolicy::getFormSectionId,
                    Function.identity(),
                    (first, second) -> second
                ));

        FormWithStructureInfo formStructure = getFormUseCase.getFormWithStructure(applicationForm.getFormId());
        Set<Long> allowedQuestionIds = new HashSet<>();
        Set<Long> requiredQuestionIds = new HashSet<>();
        for (FormWithStructureInfo.SectionWithQuestions section : formStructure.sections()) {
            ProjectApplicationFormPolicy policy = policyBySectionId.get(section.sectionId());
            if (policy == null || !policy.canAccess(applicantPart)) {
                continue;
            }

            for (FormWithStructureInfo.QuestionWithOptions question : section.questions()) {
                allowedQuestionIds.add(question.questionId());
                if (question.isRequired()) {
                    requiredQuestionIds.add(question.questionId());
                }
            }
        }
        return new VisibleQuestionScope(allowedQuestionIds, requiredQuestionIds);
    }

    private void validateAnswerQuestionIdsAllowed(
        List<UpdateProjectApplicationDraftCommand.AnswerEntry> answers,
        Set<Long> allowedQuestionIds
    ) {
        if (answers == null) {
            throw new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_INVALID_QUESTION_ID);
        }

        for (UpdateProjectApplicationDraftCommand.AnswerEntry answer : answers) {
            if (answer.questionId() == null || !allowedQuestionIds.contains(answer.questionId())) {
                throw new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_INVALID_QUESTION_ID);
            }
        }
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

    private record VisibleQuestionScope(
        Set<Long> allowedQuestionIds,
        Set<Long> requiredQuestionIds
    ) {
    }
}
