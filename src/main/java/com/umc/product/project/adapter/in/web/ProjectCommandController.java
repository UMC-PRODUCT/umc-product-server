package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.AddProjectMemberRequest;
import com.umc.product.project.adapter.in.web.dto.request.CreateDraftProjectRequest;
import com.umc.product.project.adapter.in.web.dto.request.TransferProjectOwnershipRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateProjectRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectStatusResponse;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.RemoveProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.TransferProjectOwnershipUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 Command", description = "프로젝트 및 참여자 관련 생성, 수정, 삭제 등")
public class ProjectCommandController {

    private final CreateDraftProjectUseCase createDraftProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final SubmitProjectUseCase submitProjectUseCase;
    private final TransferProjectOwnershipUseCase transferProjectOwnershipUseCase;
    private final AddProjectMemberUseCase addProjectMemberUseCase;
    private final RemoveProjectMemberUseCase removeProjectMemberUseCase;

    @PostMapping
    @Operation(
        summary = "[PROJECT-101] 프로젝트 Draft 생성",
        description = "PM(PLAN 파트 챌린저)이 빈 DRAFT 상태의 프로젝트를 생성합니다. 페이지 진입 시 GET /me/draft로 사전 확인 후 호출 권장. 동일 PM·동일 기수 중복 생성 시 409."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.WRITE,
        message = "프로젝트 생성 권한이 없습니다."
    )
    public ProjectStatusResponse createDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateDraftProjectRequest request
    ) {
        Long projectId = createDraftProjectUseCase.create(
            request.toCommand(memberPrincipal.getMemberId()));
        return ProjectStatusResponse.of(projectId, ProjectStatus.DRAFT);
    }

    @PatchMapping("/{projectId}")
    @Operation(
        summary = "[PROJECT-102] 프로젝트 기본정보 수정",
        description = "프로젝트 기본정보를 부분 업데이트합니다. DRAFT/PENDING_REVIEW/IN_PROGRESS 모두 허용, 종료 상태(COMPLETED/ABORTED)는 수정 불가. 소유권 양도는 별도 엔드포인트."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "프로젝트 수정 권한이 없습니다."
    )
    public ProjectStatusResponse update(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody UpdateProjectRequest request
    ) {
        ProjectStatus status = updateProjectUseCase.update(
            request.toCommand(projectId, memberPrincipal.getMemberId()));
        return ProjectStatusResponse.of(projectId, status);
    }

    @PostMapping("/{projectId}/submit")
    @Operation(
        summary = "[PROJECT-107] 프로젝트 제출",
        description = "DRAFT 상태의 프로젝트를 제출하여 PENDING_REVIEW로 전이합니다. 작성자 PM만 호출 가능."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "프로젝트 제출 권한이 없습니다."
    )
    public ProjectStatusResponse submit(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        submitProjectUseCase.submit(SubmitProjectCommand.builder()
            .projectId(projectId)
            .requesterMemberId(memberPrincipal.getMemberId())
            .build());
        return ProjectStatusResponse.of(projectId, ProjectStatus.PENDING_REVIEW);
    }

    @PostMapping("/{projectId}/transfer-ownership")
    @Operation(
        summary = "[PROJECT-104] 프로젝트 소유권 양도",
        description = "메인 PM을 다른 PLAN 파트 챌린저에게 양도합니다. 현재 PM만 호출 가능. 종료 상태에서는 호출 불가."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "프로젝트 소유권 양도 권한이 없습니다."
    )
    public ProjectStatusResponse transferOwnership(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody TransferProjectOwnershipRequest request
    ) {
        ProjectStatus status = transferProjectOwnershipUseCase.transfer(
            request.toCommand(projectId, memberPrincipal.getMemberId()));
        return ProjectStatusResponse.of(projectId, status);
    }

    @PostMapping("/{projectId}/members")
    @Operation(
        summary = "[PROJECT-004] 프로젝트 팀원 추가",
        description = "프로젝트에 멤버를 추가합니다. 보조 PM 추가는 part = PLAN. DRAFT 단계에선 PM 본인만, IN_PROGRESS 에선 운영진(중앙 총괄단)도 호출 가능."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "프로젝트 팀원 추가 권한이 없습니다."
    )
    public Long addMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody AddProjectMemberRequest request
    ) {
        return addProjectMemberUseCase.add(
            request.toCommand(projectId, memberPrincipal.getMemberId()));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(
        summary = "[PROJECT-005] 프로젝트 팀원 제거",
        description = "프로젝트에서 멤버를 제거합니다. DRAFT/PENDING_REVIEW 단계는 hard delete (실수 정정), IN_PROGRESS 단계는 soft delete (히스토리 보존). 메인 PM 은 양도 API 로 변경해야 합니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "프로젝트 팀원 제거 권한이 없습니다."
    )
    public void removeMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @PathVariable Long memberId,
        @RequestParam(required = false) String reason
    ) {
        removeProjectMemberUseCase.remove(RemoveProjectMemberCommand.builder()
            .projectId(projectId)
            .memberId(memberId)
            .reason(reason)
            .requesterMemberId(memberPrincipal.getMemberId())
            .build());
    }
}
