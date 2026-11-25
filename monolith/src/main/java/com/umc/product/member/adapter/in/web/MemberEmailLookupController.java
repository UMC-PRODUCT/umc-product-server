package com.umc.product.member.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.dto.request.MemberEmailLookupRequest;
import com.umc.product.member.application.port.in.query.GetMemberEmailHintUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberEmailHintInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member | 회원 Query")
public class MemberEmailLookupController {

    private final GetMemberEmailHintUseCase getMemberEmailHintUseCase;

    @Public
    @PostMapping("/email-lookup")
    @Operation(operationId = "MEMBER-107", summary = "가입 이메일 찾기", description = """
        학교 ID, 이름, 닉네임이 정확히 일치하는 회원의 마스킹된 이메일과 한국 기준 가입일을 반환합니다.
        linkedProviders는 카카오, Apple, Google 순서이며 연동 계정이 없으면 빈 배열입니다.
        탈퇴 회원은 제외하며, 일치하는 회원이 없거나 여러 명이면 404를 반환합니다.
        """)
    public MemberEmailHintInfo lookup(@Valid @RequestBody MemberEmailLookupRequest request) {
        return getMemberEmailHintUseCase.getByIdentity(request.schoolId(), request.name(), request.nickname());
    }
}
