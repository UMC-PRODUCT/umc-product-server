package com.umc.product.organization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateProductTeamGenerationRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateProductTeamGenerationRequest;
import com.umc.product.organization.application.port.in.command.ManageProductTeamGenerationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-team/generations")
@RequiredArgsConstructor
public class ProductTeamGenerationCommandController {

    private final ManageProductTeamGenerationUseCase manageProductTeamGenerationUseCase;

    @CheckAccess(
        resourceType = ResourceType.PRODUCT_TEAM,
        permission = PermissionType.WRITE,
        message = "프로덕트팀 기수를 생성할 권한이 없습니다."
    )
    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateProductTeamGenerationRequest request
    ) {
        return manageProductTeamGenerationUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @CheckAccess(
        resourceType = ResourceType.PRODUCT_TEAM,
        resourceId = "#productTeamGenerationId",
        permission = PermissionType.EDIT,
        message = "프로덕트팀 기수를 수정할 권한이 없습니다."
    )
    @PatchMapping("/{productTeamGenerationId}")
    public void update(
        @PathVariable Long productTeamGenerationId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateProductTeamGenerationRequest request
    ) {
        manageProductTeamGenerationUseCase.update(
            request.toCommand(productTeamGenerationId, currentMemberId(currentMember))
        );
    }

    @CheckAccess(
        resourceType = ResourceType.PRODUCT_TEAM,
        resourceId = "#productTeamGenerationId",
        permission = PermissionType.DELETE,
        message = "프로덕트팀 기수를 삭제할 권한이 없습니다."
    )
    @DeleteMapping("/{productTeamGenerationId}")
    public void delete(
        @PathVariable Long productTeamGenerationId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageProductTeamGenerationUseCase.delete(productTeamGenerationId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
