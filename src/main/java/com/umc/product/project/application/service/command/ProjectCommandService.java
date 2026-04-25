package com.umc.product.project.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.TransferProjectOwnershipUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.TransferProjectOwnershipCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
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
    TransferProjectOwnershipUseCase {

    private final LoadProjectPort loadProjectPort;
    private final SaveProjectPort saveProjectPort;

    // Cross-domain UseCases
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

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

        Project project = Project.createDraft(command.gisuId(), chapter.id(), command.productOwnerMemberId());
        return saveProjectPort.save(project).getId();
    }

    @Override
    public void update(UpdateProjectCommand command) {
        Project project = loadProjectPort.getById(command.projectId());
        project.updateBasicInfo(
            command.name(),
            command.description(),
            command.externalLink(),
            command.thumbnailFileId(),
            command.logoFileId()
        );
    }

    @Override
    public void submit(SubmitProjectCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (!project.getProductOwnerMemberId().equals(command.requesterMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        project.submit();
    }

    @Override
    public void transfer(TransferProjectOwnershipCommand command) {
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

        project.transferOwnership(command.newOwnerMemberId());
    }
}
