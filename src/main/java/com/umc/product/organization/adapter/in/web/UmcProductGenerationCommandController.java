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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/generations")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC Product 기수 Command", description = "UMC Product 기수 생성, 수정, 삭제")
public class UmcProductGenerationCommandController {

    private final ManageUmcProductGenerationUseCase manageUmcProductGenerationUseCase;

    @PostMapping
    @Operation(
        operationId = "UMC-PRODUCT-GENERATION-001",
        summary = "[UMC-PRODUCT-GENERATION-001] UMC Product 기수 생성",
        description = "UMC Product 기수를 생성합니다. 기수 생성 권한을 가진 요청자만 호출할 수 있으며, 동일한 기수 번호는 중복 등록할 수 없습니다. active=true로 생성하면 기존 활성 기수는 비활성화됩니다."
    )
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductGenerationRequest request
    ) {
        return manageUmcProductGenerationUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{umcProductGenerationId}")
    @Operation(
        operationId = "UMC-PRODUCT-GENERATION-002",
        summary = "[UMC-PRODUCT-GENERATION-002] UMC Product 기수 수정",
        description = "UMC Product 기수의 번호, 운영 기간, 활성 여부를 수정합니다. 해당 기수 관리 권한을 가진 요청자만 호출할 수 있으며, active=true로 수정하면 기존 활성 기수는 비활성화됩니다."
    )
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
    @Operation(
        operationId = "UMC-PRODUCT-GENERATION-003",
        summary = "[UMC-PRODUCT-GENERATION-003] UMC Product 기수 삭제",
        description = "UMC Product 기수를 삭제합니다. 해당 기수 관리 권한을 가진 요청자만 호출할 수 있습니다."
    )
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
