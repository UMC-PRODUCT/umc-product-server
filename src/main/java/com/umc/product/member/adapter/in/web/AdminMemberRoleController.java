package com.umc.product.member.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.adapter.in.web.dto.request.UpdateMemberRoleRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.application.port.in.command.ManageMemberRoleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/member")
@RequiredArgsConstructor
@Tag(name = "Member | 회원 권한 관리", description = "전역 관리자 권한으로 회원 권한을 관리합니다.")
public class AdminMemberRoleController {

    private final ManageMemberRoleUseCase manageMemberRoleUseCase;
    private final MemberInfoResponseAssembler assembler;

    @PatchMapping("/{memberId}/role")
    @Operation(summary = "[MEMBER-ADMIN-001] 회원 전역 권한 변경")
    @CheckAccess(
        resourceType = ResourceType.MEMBER,
        resourceId = "#memberId",
        permission = PermissionType.MANAGE
    )
    public MemberInfoResponse updateRole(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long memberId,
        @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        manageMemberRoleUseCase.updateRole(request.toCommand(memberPrincipal.getMemberId(), memberId));
        return assembler.fromMemberId(memberId);
    }
}
