package com.umc.product.member.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.request.EditBusinessCardRequest;
import com.umc.product.member.adapter.in.web.dto.response.BusinessCardEditResponse;
import com.umc.product.member.adapter.in.web.dto.response.BusinessCardResponse;
import com.umc.product.member.application.port.in.command.EditBusinessCardUseCase;
import com.umc.product.member.application.port.in.query.GetBusinessCardUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/me/business-card")
@RequiredArgsConstructor
@Tag(name = "Member | 명함", description = "회원 프로필로 구성한 명함을 조회하고 수정합니다.")
public class MemberBusinessCardController {

    private final GetBusinessCardUseCase getBusinessCardUseCase;
    private final EditBusinessCardUseCase editBusinessCardUseCase;

    @Operation(operationId = "MEMBER-104", summary = "내 명함 조회",
        description = """
            마이페이지 기본 화면(명함)에 사용합니다. 다른 회원에게 보이는 그대로를 반환합니다.

            - 기본 정보: 이름, 닉네임, 학교, 프로필 사진
            - `activity`: 활동 이력 중 가장 최신 기수의 기수·파트. 활동 이력이 없으면 `null`
            - `contact`: 연락처(이메일). 공개로 설정했고 입력한 경우에만 포함. 그 외에는 `null`. 로그인 이메일과 별개입니다.
            - `links`: 공개로 설정했고 http/https 형식인 링크만 포함
            """)
    @GetMapping
    BusinessCardResponse getMyBusinessCard(@CurrentMember MemberPrincipal memberPrincipal) {
        return BusinessCardResponse.from(getBusinessCardUseCase.getByMemberId(memberPrincipal.getMemberId()));
    }

    @Operation(operationId = "MEMBER-105", summary = "내 명함 수정 화면 조회",
        description = """
            명함 수정 화면에 사용합니다. 비공개 항목도 공개 여부와 함께 반환합니다.

            - 수정 불가 항목: 이름, 학교, `activity`(최신 기수·파트), `oauthProviders`(연동된 소셜)
            - 수정 가능 항목: 프로필 사진, `contact`, `links`
            - `links`: 링크 타입마다 하나씩 항상 5개. 입력하지 않은 링크는 `link`가 `null`
            """)
    @GetMapping("/edit")
    BusinessCardEditResponse getMyBusinessCardEdit(@CurrentMember MemberPrincipal memberPrincipal) {
        return BusinessCardEditResponse.from(getBusinessCardUseCase.getEditByMemberId(memberPrincipal.getMemberId()));
    }

    @Operation(operationId = "MEMBER-006", summary = "내 명함 수정",
        description = """
            프로필 사진·연락처·링크와 각 공개 여부를 한 번에 저장하고, 명함 수정 화면 조회와 같은 응답을 반환합니다.

            - `profileImageId`: 생략하면 기존 사진을 유지합니다.
            - `contact`: 필수. 이메일을 비우면 삭제합니다.
            - `links`: 필수. 링크 전체를 교체하며, 목록에 없는 타입의 링크는 삭제됩니다. 타입은 중복될 수 없습니다.
            - `isPublic`: 연락처와 각 링크에 필수입니다.
            """)
    @PatchMapping
    BusinessCardEditResponse editMyBusinessCard(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody @Valid EditBusinessCardRequest request
    ) {
        Long memberId = memberPrincipal.getMemberId();
        editBusinessCardUseCase.edit(request.toCommand(memberId));
        return BusinessCardEditResponse.from(getBusinessCardUseCase.getEditByMemberId(memberId));
    }
}
