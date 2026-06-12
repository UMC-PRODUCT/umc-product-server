package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductFunctionalUnitRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductFunctionalUnitRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductFunctionalUnitUseCase;
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
@RequestMapping("/api/v1/umc-product/functional-units")
@RequiredArgsConstructor
public class UmcProductFunctionalUnitCommandController {

    private final ManageUmcProductFunctionalUnitUseCase manageUmcProductFunctionalUnitUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductFunctionalUnitRequest request
    ) {
        return manageUmcProductFunctionalUnitUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{functionalUnitId}")
    public void update(
        @PathVariable Long functionalUnitId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateUmcProductFunctionalUnitRequest request
    ) {
        manageUmcProductFunctionalUnitUseCase.update(
            request.toCommand(functionalUnitId, currentMemberId(currentMember))
        );
    }

    @DeleteMapping("/{functionalUnitId}")
    public void delete(
        @PathVariable Long functionalUnitId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageUmcProductFunctionalUnitUseCase.delete(functionalUnitId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
