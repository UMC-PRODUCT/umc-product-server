package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateProductTeamSquadRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceProductTeamSquadParticipantsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateProductTeamSquadRequest;
import com.umc.product.organization.application.port.in.command.ManageProductTeamSquadUseCase;
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
@RequestMapping("/api/v1/product-team/squads")
@RequiredArgsConstructor
public class ProductTeamSquadCommandController {

    private final ManageProductTeamSquadUseCase manageProductTeamSquadUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateProductTeamSquadRequest request
    ) {
        return manageProductTeamSquadUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{squadId}")
    public void update(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateProductTeamSquadRequest request
    ) {
        manageProductTeamSquadUseCase.update(request.toCommand(squadId, currentMemberId(currentMember)));
    }

    @DeleteMapping("/{squadId}")
    public void delete(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageProductTeamSquadUseCase.delete(squadId, currentMemberId(currentMember));
    }

    @PutMapping("/{squadId}/participants")
    public void replaceParticipants(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid ReplaceProductTeamSquadParticipantsRequest request
    ) {
        manageProductTeamSquadUseCase.replaceParticipants(request.toCommand(squadId, currentMemberId(currentMember)));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
