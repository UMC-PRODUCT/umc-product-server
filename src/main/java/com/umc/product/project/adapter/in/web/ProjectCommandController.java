package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.CreateDraftProjectRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateProjectRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectStatusResponse;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 Command", description = "프로젝트 및 참여자 관련 생성, 수정, 삭제 등")
public class ProjectCommandController {

    private final CreateDraftProjectUseCase createDraftProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final SubmitProjectUseCase submitProjectUseCase;
    private final GetProjectUseCase getProjectUseCase;

    @PostMapping
    @Operation(
        summary = "프로젝트 Draft 생성 (PROJECT-101)",
        description = "PM(PLAN 파트 챌린저)이 DRAFT 상태의 프로젝트를 생성합니다. 이미 같은 기수에 프로젝트가 있으면 409를 반환합니다."
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
        summary = "프로젝트 기본정보 수정 (PROJECT-102)",
        description = "프로젝트 기본정보를 부분 업데이트합니다. null 필드는 수정하지 않습니다."
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
        updateProjectUseCase.update(request.toCommand(projectId, memberPrincipal.getMemberId()));
        ProjectInfo info = getProjectUseCase.getById(projectId);
        return ProjectStatusResponse.from(info);
    }

    @PostMapping("/{projectId}/submit")
    @Operation(
        summary = "프로젝트 제출 (PROJECT-107)",
        description = "DRAFT 상태의 프로젝트를 제출하여 PENDING_REVIEW로 전이합니다."
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
}
