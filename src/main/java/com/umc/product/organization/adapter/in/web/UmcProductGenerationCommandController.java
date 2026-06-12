package com.umc.product.organization.adapter.in.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductGenerationRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductGenerationRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductGenerationUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/generations")
@RequiredArgsConstructor
public class UmcProductGenerationCommandController {

    private final ManageUmcProductGenerationUseCase manageUmcProductGenerationUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductGenerationRequest request
    ) {
        return manageUmcProductGenerationUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{umcProductGenerationId}")
    public void update(
        @PathVariable Long umcProductGenerationId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateUmcProductGenerationRequest request
    ) {
        manageUmcProductGenerationUseCase.update(
            request.toCommand(umcProductGenerationId, currentMemberId(currentMember))
        );
    }

    @DeleteMapping("/{umcProductGenerationId}")
    public void delete(
        @PathVariable Long umcProductGenerationId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageUmcProductGenerationUseCase.delete(umcProductGenerationId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
