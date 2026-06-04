package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateProductTeamMemberRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceProductTeamMemberActivitiesRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateProductTeamMemberProfileRequest;
import com.umc.product.organization.application.port.in.command.ManageProductTeamMemberUseCase;
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
@RequestMapping("/api/v1/product-team/members")
@RequiredArgsConstructor
public class ProductTeamMemberCommandController {

    private final ManageProductTeamMemberUseCase manageProductTeamMemberUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateProductTeamMemberRequest request
    ) {
        return manageProductTeamMemberUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{productTeamMemberId}/profile")
    public void updateProfile(
        @PathVariable Long productTeamMemberId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateProductTeamMemberProfileRequest request
    ) {
        manageProductTeamMemberUseCase.updateProfile(
            request.toCommand(productTeamMemberId, currentMemberId(currentMember))
        );
    }

    @PutMapping("/{productTeamMemberId}/activities")
    public void replaceActivities(
        @PathVariable Long productTeamMemberId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid ReplaceProductTeamMemberActivitiesRequest request
    ) {
        manageProductTeamMemberUseCase.replaceActivities(
            request.toCommand(productTeamMemberId, currentMemberId(currentMember))
        );
    }

    @DeleteMapping("/{productTeamMemberId}")
    public void delete(
        @PathVariable Long productTeamMemberId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageProductTeamMemberUseCase.delete(productTeamMemberId, currentMemberId(currentMember));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
