package com.umc.product.organization.adapter.in.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductSquadRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceUmcProductSquadParticipantsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductSquadRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductSquadUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/squads")
@RequiredArgsConstructor
public class UmcProductSquadCommandController {

    private final ManageUmcProductSquadUseCase manageUmcProductSquadUseCase;

    @PostMapping
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductSquadRequest request
    ) {
        return manageUmcProductSquadUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{squadId}")
    public void update(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateUmcProductSquadRequest request
    ) {
        manageUmcProductSquadUseCase.update(request.toCommand(squadId, currentMemberId(currentMember)));
    }

    @DeleteMapping("/{squadId}")
    public void delete(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageUmcProductSquadUseCase.delete(squadId, currentMemberId(currentMember));
    }

    @PutMapping("/{squadId}/participants")
    public void replaceParticipants(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid ReplaceUmcProductSquadParticipantsRequest request
    ) {
        manageUmcProductSquadUseCase.replaceParticipants(request.toCommand(squadId, currentMemberId(currentMember)));
    }

    private Long currentMemberId(MemberPrincipal currentMember) {
        if (currentMember == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return currentMember.getMemberId();
    }
}
