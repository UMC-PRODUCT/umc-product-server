package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateProductTeamFunctionalUnitRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateProductTeamFunctionalUnitRequest;
import com.umc.product.organization.application.port.in.command.ManageProductTeamFunctionalUnitUseCase;
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
@RequestMapping("/api/v1/product-team/functional-units")
@RequiredArgsConstructor
public class ProductTeamFunctionalUnitCommandController {

    private final ManageProductTeamFunctionalUnitUseCase manageProductTeamFunctionalUnitUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateProductTeamFunctionalUnitRequest request
    ) {
        return manageProductTeamFunctionalUnitUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{functionalUnitId}")
    public void update(
        @PathVariable Long functionalUnitId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateProductTeamFunctionalUnitRequest request
    ) {
        manageProductTeamFunctionalUnitUseCase.update(
            request.toCommand(functionalUnitId, currentMemberId(currentMember))
        );
    }

    @DeleteMapping("/{functionalUnitId}")
    public void delete(
        @PathVariable Long functionalUnitId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageProductTeamFunctionalUnitUseCase.delete(functionalUnitId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
