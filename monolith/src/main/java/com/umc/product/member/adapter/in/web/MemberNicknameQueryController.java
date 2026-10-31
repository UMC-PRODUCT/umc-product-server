package com.umc.product.member.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.dto.request.CheckNicknameAvailabilityRequest;
import com.umc.product.member.adapter.in.web.dto.response.NicknameAvailabilityResponse;
import com.umc.product.member.application.port.in.query.CheckNicknameAvailabilityUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/nicknames")
@RequiredArgsConstructor
@Tag(name = "Member | 회원 Query", description = "회원 정보와 검색 결과를 조회합니다.")
public class MemberNicknameQueryController {

    private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;

    @Public
    @GetMapping("/availability")
    @Operation(operationId = "MEMBER-106", summary = "학교별 닉네임 사용 가능 여부 확인", description = """
        같은 학교의 모든 저장 회원을 대상으로 닉네임 사용 가능 여부를 조회합니다.
        기수, 챌린저 등록 여부, 회원 상태(탈퇴 포함)로 제한하지 않습니다.
        다른 학교의 동일 닉네임은 사용할 수 있습니다.

        중복이어도 200 응답의 available=false로 안내하며 회원 개인정보는 반환하지 않습니다.
        이 결과는 조회 시점의 안내로, 닉네임 예약이나 최종 회원가입 성공을 보장하지 않습니다.
        """)
    public NicknameAvailabilityResponse checkNicknameAvailability(
        @ParameterObject @Valid @ModelAttribute CheckNicknameAvailabilityRequest request
    ) {
        return NicknameAvailabilityResponse.from(
            checkNicknameAvailabilityUseCase.check(request.schoolId(), request.nickname()));
    }
}
