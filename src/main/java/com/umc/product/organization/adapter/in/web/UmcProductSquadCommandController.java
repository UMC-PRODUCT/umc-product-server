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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/squads")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC Product 스쿼드 Command", description = "UMC Product 스쿼드 생성, 수정, 삭제, 참여자 관리")
public class UmcProductSquadCommandController {

    private final ManageUmcProductSquadUseCase manageUmcProductSquadUseCase;

    @PostMapping
    @Operation(
        operationId = "UMC-PRODUCT-SQUAD-001",
        summary = "[UMC-PRODUCT-SQUAD-001] UMC Product 스쿼드 생성",
        description = "UMC Product 스쿼드를 생성합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 코드, 이름, 운영 기간, 정렬 순서, 활성 여부를 저장합니다."
    )
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductSquadRequest request
    ) {
        return manageUmcProductSquadUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{squadId}")
    @Operation(
        operationId = "UMC-PRODUCT-SQUAD-002",
        summary = "[UMC-PRODUCT-SQUAD-002] UMC Product 스쿼드 수정",
        description = "UMC Product 스쿼드의 기본 정보를 수정합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 요청 본문의 값으로 코드, 이름, 설명, 운영 기간, 정렬 순서, 활성 여부를 갱신합니다."
    )
    public void update(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid UpdateUmcProductSquadRequest request
    ) {
        manageUmcProductSquadUseCase.update(request.toCommand(squadId, currentMemberId(currentMember)));
    }

    @DeleteMapping("/{squadId}")
    @Operation(
        operationId = "UMC-PRODUCT-SQUAD-003",
        summary = "[UMC-PRODUCT-SQUAD-003] UMC Product 스쿼드 삭제",
        description = "UMC Product 스쿼드를 삭제합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 연결된 스쿼드 참여자 정보도 함께 삭제됩니다."
    )
    public void delete(
        @PathVariable Long squadId,
        @CurrentMember MemberPrincipal currentMember
    ) {
        manageUmcProductSquadUseCase.delete(squadId, currentMemberId(currentMember));
    }

    @PutMapping("/{squadId}/participants")
    @Operation(
        operationId = "UMC-PRODUCT-SQUAD-004",
        summary = "[UMC-PRODUCT-SQUAD-004] UMC Product 스쿼드 참여자 교체",
        description = "UMC Product 스쿼드 참여자 목록을 요청 본문 기준으로 전체 교체합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 기존 참여자는 삭제 후 새 참여자로 저장됩니다."
    )
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
