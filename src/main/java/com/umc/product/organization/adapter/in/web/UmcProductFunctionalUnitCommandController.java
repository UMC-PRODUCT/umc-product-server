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
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductFunctionalUnitRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductFunctionalUnitRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductFunctionalUnitUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/functional-units")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC PRODUCT 기능 조직 Command", description = "UMC PRODUCT 기능 조직 생성, 수정, 삭제")
public class UmcProductFunctionalUnitCommandController {

    private final ManageUmcProductFunctionalUnitUseCase manageUmcProductFunctionalUnitUseCase;

    @PostMapping
    @Operation(
        operationId = "UMC-PRODUCT-FUNCTIONAL-UNIT-001",
        summary = "UMC PRODUCT 기능 조직 생성",
        description = "UMC PRODUCT 기수에 속한 기능 조직을 생성합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 상위 기능 조직을 지정하는 경우 같은 기수 안의 조직이어야 합니다."
    )
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductFunctionalUnitRequest request
    ) {
        return manageUmcProductFunctionalUnitUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{functionalUnitId}")
    @Operation(
        operationId = "UMC-PRODUCT-FUNCTIONAL-UNIT-002",
        summary = "UMC PRODUCT 기능 조직 수정",
        description = "UMC PRODUCT 기능 조직의 상위 조직, 유형, 코드, 이름, 설명, 정렬 순서, 활성 여부를 수정합니다. 운영 권한을 가진 요청자만 호출할 수 있습니다."
    )
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
    @Operation(
        operationId = "UMC-PRODUCT-FUNCTIONAL-UNIT-003",
        summary = "UMC PRODUCT 기능 조직 삭제",
        description = "UMC PRODUCT 기능 조직을 삭제합니다. 운영 권한을 가진 요청자만 호출할 수 있습니다."
    )
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
