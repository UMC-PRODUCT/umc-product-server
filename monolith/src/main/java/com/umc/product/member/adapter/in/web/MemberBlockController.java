package com.umc.product.member.adapter.in.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.response.BlockedMemberListResponse;
import com.umc.product.member.adapter.in.web.dto.response.MemberBlockResponse;
import com.umc.product.member.application.port.in.command.BlockMemberUseCase;
import com.umc.product.member.application.port.in.command.UnblockMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.MemberBlockCommand;
import com.umc.product.member.application.port.in.query.ListBlockedMembersUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/blocks")
@RequiredArgsConstructor
@Tag(name = "Member | 차단", description = "회원 간 차단/차단 해제/차단 목록 조회를 다룹니다.")
public class MemberBlockController {

    private final BlockMemberUseCase blockMemberUseCase;
    private final UnblockMemberUseCase unblockMemberUseCase;
    private final ListBlockedMembersUseCase listBlockedMembersUseCase;

    @PostMapping("/{memberId}")
    @Operation(operationId = "MEMBER-BLOCK-001", summary = "회원 차단")
    public MemberBlockResponse block(
        @PathVariable Long memberId,
        @CurrentMember MemberPrincipal principal
    ) {
        return MemberBlockResponse.from(blockMemberUseCase.block(
            new MemberBlockCommand(principal.getMemberId(), memberId)
        ));
    }

    @DeleteMapping("/{memberId}")
    @Operation(operationId = "MEMBER-BLOCK-002", summary = "회원 차단 해제")
    public MemberBlockResponse unblock(
        @PathVariable Long memberId,
        @CurrentMember MemberPrincipal principal
    ) {
        return MemberBlockResponse.from(unblockMemberUseCase.unblock(
            new MemberBlockCommand(principal.getMemberId(), memberId)
        ));
    }

    @GetMapping
    @Operation(operationId = "MEMBER-BLOCK-003", summary = "내가 차단한 회원 목록 조회")
    public BlockedMemberListResponse listBlocked(@CurrentMember MemberPrincipal principal) {
        return BlockedMemberListResponse.from(
            listBlockedMembersUseCase.listBlockedMembers(principal.getMemberId())
        );
    }
}
