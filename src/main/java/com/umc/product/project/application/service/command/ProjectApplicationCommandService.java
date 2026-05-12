package com.umc.product.project.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.DecideApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectApplicationCommandService implements
    CreateDraftProjectApplicationUseCase,
    UpdateProjectApplicationDraftUseCase,
    SubmitProjectApplicationUseCase,
    DecideApplicationUseCase {

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

        switch (targetStatus) {
            case APPROVED -> application.approve(decidedByMemberId, reason);
            case REJECTED -> application.reject(decidedByMemberId, reason);
            case PENDING -> application.revertToPending(decidedByMemberId);
        }

        saveProjectApplicationPort.save(application);
        return ProjectApplicationInfo.of(application.getId(), application.getStatus());
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
