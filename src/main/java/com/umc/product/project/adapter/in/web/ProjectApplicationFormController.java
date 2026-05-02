package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.UpsertApplicationFormRequest;
import com.umc.product.project.adapter.in.web.dto.response.GetApplicationFormResponse;
import com.umc.product.project.adapter.in.web.dto.response.UpsertApplicationFormResponse;
import com.umc.product.project.application.port.in.command.UpsertProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 지원 폼", description = "프로젝트 지원 폼 저장 / 조회 (PROJECT-106)")
public class ProjectApplicationFormController {

    private final UpsertProjectApplicationFormUseCase upsertProjectApplicationFormUseCase;
    private final GetProjectApplicationFormUseCase getProjectApplicationFormUseCase;

    @PutMapping("/{projectId}/application-form")
    @Operation(
        summary = "[PROJECT-106] 지원 폼 저장",
        description = "본문이 곧 폼의 새 상태가 된다 (PUT 시멘틱). 폼이 없으면 생성하고, 있으면 섹션/질문/옵션을 본문 구조와 일치하도록 동기화한다. DRAFT/PENDING_REVIEW 상태에서만 호출 가능."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.EDIT,
        message = "지원 폼 저장 권한이 없습니다."
    )
    public UpsertApplicationFormResponse upsert(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody UpsertApplicationFormRequest request
    ) {
        ApplicationFormInfo info = upsertProjectApplicationFormUseCase.upsert(
            request.toCommand(projectId, memberPrincipal.getMemberId()));
        return UpsertApplicationFormResponse.from(info);
    }

    // TODO: 챌린저 호출 시 본인 파트 + COMMON 섹션 마스킹 적용 (response.forApplicant(applicantPart))
    @GetMapping("/{projectId}/application-form")
    @Operation(
        summary = "[PROJECT-106-GET] 지원 폼 조회",
        description = "프로젝트의 지원 폼 전체 구조를 조회한다. 폼이 없으면 ApiResponse.result = null."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "지원 폼 조회 권한이 없습니다."
    )
    public GetApplicationFormResponse get(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return getProjectApplicationFormUseCase.findByProjectId(projectId)
            .map(GetApplicationFormResponse::from)
            .orElse(null);
    }
}
