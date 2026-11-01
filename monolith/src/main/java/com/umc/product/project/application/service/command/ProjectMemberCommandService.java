package com.umc.product.project.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.ChangeProjectMemberStatusUseCase;
import com.umc.product.project.application.port.in.command.ManageProjectMatchingUseCase;
import com.umc.product.project.application.port.in.command.RemoveProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.ChangeProjectMemberStatusCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberCommandService
    implements AddProjectMemberUseCase, RemoveProjectMemberUseCase, ChangeProjectMemberStatusUseCase {

    private final ManageProjectMatchingUseCase manageProjectMatchingUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final SaveProjectMemberPort saveProjectMemberPort;

    @Override
    public Long add(AddProjectMemberCommand command) {
        if (command.part() != ChallengerPart.PLAN) {
            return manageProjectMatchingUseCase.assign(command);
        }
        Project project = loadProjectPort.getById(command.projectId());
        project.validateMutable();

        loadProjectMemberPort.findByProjectIdAndMemberId(command.projectId(), command.memberId())
            .ifPresent(existing -> {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
            });

        if (getChallengerUseCase.getActiveByMemberIdAndGisuId(command.memberId(), project.getGisuId()).part()
            != ChallengerPart.PLAN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }
        ProjectMember member = ProjectMember.create(
            project, command.memberId(), command.part(), command.requesterMemberId());
        return saveProjectMemberPort.save(member).getId();
    }

    /**
     * 프로젝트 팀원의 배정을 해제한다.
     * <ul>
     *   <li>일반 파트: 수동 매칭 서비스에서 기간을 검증하고, 즉시 해제하거나 최종 결과 공개까지 해제를 예약한다.</li>
     *   <li>보조 PM(PLAN): 팀원 행을 즉시 삭제한다.</li>
     *   <li>COMPLETED / ABORTED: 거부 — 종료된 프로젝트의 참여 이력 보존</li>
     * </ul>
     * 메인 PM 은 양도 API 로 변경해야 하므로 본 메서드에서 거부한다.
     * 보조 PM 해제 사유는 삭제 직전 로그로 남기며, 일반 파트의 사유 처리는 수동 매칭 서비스에 위임한다.
     */
    @Override
    public void remove(RemoveProjectMemberCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (project.isOwner(command.memberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER);
        }

        // COMPLETED / ABORTED 거부. 추후 ABORTED 만 허용하려면 이 가드를 COMPLETED 단독 검사로 교체한다.
        project.validateMutable();

        ProjectMember member = loadProjectMemberPort
            .getByProjectIdAndMemberId(command.projectId(), command.memberId());

        if (member.getPart() != ChallengerPart.PLAN) {
            manageProjectMatchingUseCase.unassign(command);
            return;
        }

        log.info(
            "[ProjectMember hardDelete] projectId={}, memberId={}, projectMemberId={}, requesterMemberId={}, reason={}",
            command.projectId(), command.memberId(), member.getId(), command.requesterMemberId(), command.reason());

        saveProjectMemberPort.hardDelete(member.getId());
    }

    /**
     * 팀원의 활동 완료·중도 이탈·퇴출을 처리하고, 행을 보존한 채 사유·주체를 기록한다.
     * <ul>
     *   <li>메인 PM: 거부 — 양도 API 로 유도</li>
     *   <li>COMPLETED / ABORTED 프로젝트: 거부 — 종료된 프로젝트의 이력 고정</li>
     * </ul>
     * 동일한 종료 상태를 다시 요청하면 기존 처리 정보를 유지한다.
     */
    @Override
    public void changeStatus(ChangeProjectMemberStatusCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (project.isOwner(command.memberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER);
        }

        project.validateMutable();

        ProjectMember member = loadProjectMemberPort
            .getByProjectIdAndMemberId(command.projectId(), command.memberId());

        if (member.getPart() != ChallengerPart.PLAN) {
            manageProjectMatchingUseCase.changeStatus(command);
            return;
        }
        if (member.changeStatus(command.status(), command.requesterMemberId(), command.reason())) {
            saveProjectMemberPort.save(member);
        }
    }
}
