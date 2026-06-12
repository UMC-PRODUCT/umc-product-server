package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductMemberRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceUmcProductMemberFunctionalMembershipsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductMemberProfileRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductMemberUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/umc-product/members")
@RequiredArgsConstructor
public class UmcProductMemberCommandController {

    private final ManageUmcProductMemberUseCase manageUmcProductMemberUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductMemberRequest request
    ) {
        return manageUmcProductMemberUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{umcProductMemberId}/profile")
    public void updateProfile(
        @PathVariable Long umcProductMemberId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateUmcProductMemberProfileRequest request
    ) {
        manageUmcProductMemberUseCase.updateProfile(
            request.toCommand(umcProductMemberId, currentMemberId(currentMember))
        );
    }

    @PutMapping("/{umcProductMemberId}/functional-memberships")
    public void replaceFunctionalMemberships(
        @PathVariable Long umcProductMemberId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid ReplaceUmcProductMemberFunctionalMembershipsRequest request
    ) {
        manageUmcProductMemberUseCase.replaceFunctionalMemberships(
            request.toCommand(umcProductMemberId, currentMemberId(currentMember))
        );
    }

    @DeleteMapping("/{umcProductMemberId}")
    public void delete(
        @PathVariable Long umcProductMemberId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageUmcProductMemberUseCase.delete(umcProductMemberId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
