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
import com.umc.product.organization.adapter.in.web.dto.request.CreateUmcProductMemberRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceUmcProductMemberFunctionalMembershipsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateUmcProductMemberProfileRequest;
import com.umc.product.organization.application.port.in.command.ManageUmcProductMemberUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/umc-product/members")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC Product 멤버 Command", description = "UMC Product 멤버 생성, 프로필 수정, 소속 관리, 삭제")
public class UmcProductMemberCommandController {

    private final ManageUmcProductMemberUseCase manageUmcProductMemberUseCase;

    @PostMapping
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-001",
        summary = "[UMC-PRODUCT-MEMBER-001] UMC Product 멤버 생성",
        description = "UMC Product 조직에 멤버를 등록합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 기본 프로필과 기능 조직 소속, 스쿼드 참여 정보를 함께 저장합니다."
    )
    public Long create(
        @CurrentMember MemberPrincipal currentMember,
        @RequestBody @Valid CreateUmcProductMemberRequest request
    ) {
        return manageUmcProductMemberUseCase.create(request.toCommand(currentMemberId(currentMember)));
    }

    @PatchMapping("/{umcProductMemberId}/profile")
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-002",
        summary = "[UMC-PRODUCT-MEMBER-002] UMC Product 멤버 프로필 수정",
        description = "UMC Product 멤버의 소개와 Product 전용 프로필 이미지를 수정합니다. 본인 또는 멤버 프로필 관리 권한을 가진 요청자만 호출할 수 있습니다."
    )
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
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-003",
        summary = "[UMC-PRODUCT-MEMBER-003] UMC Product 멤버 기능 조직 소속 교체",
        description = "UMC Product 멤버의 기능 조직 소속 목록을 요청 본문 기준으로 전체 교체합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 기존 소속은 삭제 후 새 소속으로 저장됩니다."
    )
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
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-004",
        summary = "[UMC-PRODUCT-MEMBER-004] UMC Product 멤버 삭제",
        description = "UMC Product 멤버를 삭제합니다. 운영 권한을 가진 요청자만 호출할 수 있으며, 연결된 기능 조직 소속과 스쿼드 참여 정보도 함께 삭제됩니다."
    )
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
