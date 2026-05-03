package com.umc.product.project.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.TransferProjectOwnershipUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.TransferProjectOwnershipCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormUseCase;
import com.umc.product.survey.application.port.in.command.dto.PublishFormCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandService implements
    CreateDraftProjectUseCase,
    UpdateProjectUseCase,
    SubmitProjectUseCase,
    TransferProjectOwnershipUseCase,
    PublishProjectUseCase {

    private final LoadProjectPort loadProjectPort;
    private final SaveProjectPort saveProjectPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;

    // Cross-domain UseCases
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final ManageFormUseCase manageFormUseCase;

    @Override
    public Long create(CreateDraftProjectCommand command) {
        getGisuUseCase.getById(command.gisuId());

        ChallengerInfo challenger = getChallengerUseCase.getByMemberIdAndGisuId(
            command.productOwnerMemberId(), command.gisuId()
        );
        if (challenger.part() != ChallengerPart.PLAN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }

        if (loadProjectPort.existsByOwnerAndGisu(command.productOwnerMemberId(), command.gisuId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_DUPLICATE_IN_GISU);
        }

        MemberInfo member = getMemberUseCase.getById(command.productOwnerMemberId());
        ChapterInfo chapter = getChapterUseCase.byGisuAndSchool(command.gisuId(), member.schoolId());

        Project project = Project.createDraft(
            command.gisuId(),
            chapter.id(),
            command.productOwnerMemberId(),
            member.schoolId()
        );
        return saveProjectPort.save(project).getId();
    }

    @Override
    public ProjectStatus update(UpdateProjectCommand command) {
        Project project = loadProjectPort.getById(command.projectId());
        project.updateBasicInfo(
            command.name(),
            command.description(),
            command.externalLink(),
            command.thumbnailFileId(),
            command.logoFileId()
        );
        return project.getStatus();
    }

    @Override
    public void submit(SubmitProjectCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (!project.getProductOwnerMemberId().equals(command.requesterMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        if (!loadProjectApplicationFormPort.existsByProjectId(project.getId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }

        project.submit();
    }

    @Override
    public ProjectStatus transfer(TransferProjectOwnershipCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (!project.getProductOwnerMemberId().equals(command.requesterMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        ChallengerInfo newOwner = getChallengerUseCase.getByMemberIdAndGisuId(
            command.newOwnerMemberId(), project.getGisuId()
        );
        if (newOwner.part() != ChallengerPart.PLAN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }

        if (loadProjectPort.existsByOwnerAndGisu(command.newOwnerMemberId(), project.getGisuId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_DUPLICATE_IN_GISU);
        }

        // 도메인 가드 fail-fast — COMPLETED/ABORTED 시 cross-domain 호출 회피
        project.validateMutable();

        MemberInfo newOwnerMember = getMemberUseCase.getById(command.newOwnerMemberId());
        ChapterInfo newChapter = getChapterUseCase.byGisuAndSchool(
            project.getGisuId(), newOwnerMember.schoolId());
        project.transferOwnership(
            command.newOwnerMemberId(), newOwnerMember.schoolId(), newChapter.id());
        return project.getStatus();
    }

    /**
     * Admin 검토 완료 후 공개. PENDING_REVIEW → IN_PROGRESS 전이 + 지원 폼 동반 publish.
     * <ul>
     *   <li>파트별 정원이 1개 이상 등록되어 있어야 함 (PROJECT-105 선행)</li>
     *   <li>지원 폼이 등록되어 있어야 함 (PROJECT-106 선행)</li>
     *   <li>같은 트랜잭션에서 Form 도 PUBLISHED 로 전이</li>
     * </ul>
     */
    @Override
    public ProjectStatus publish(PublishProjectCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        List<ProjectPartQuota> quotas = loadProjectPartQuotaPort.listByProjectId(project.getId());
        if (quotas.isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_REQUIRED);
        }

        ProjectApplicationForm form = loadProjectApplicationFormPort.findByProjectId(project.getId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND));

        project.publish();

        manageFormUseCase.publishForm(PublishFormCommand.builder()
            .formId(form.getFormId())
            .requesterMemberId(command.requesterMemberId())
            .build());

        return project.getStatus();
    }
}
