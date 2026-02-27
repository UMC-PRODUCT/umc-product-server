package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.AddOAuthRequest;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.in.query.GetOAuthListUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.OAuthVerificationClaims;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member-oauth")
@RequiredArgsConstructor
@Tag(name = "Authentication | OAuth 연동", description = "Member에 OAuth 계정을 연동하거나 제거하는 API")
public class MemberOAuthController {

    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final GetOAuthListUseCase oAuthListUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "로그인용 OAuth 수단 추가",
        description = "같은 OAuth Provider도 여러 개 추가할 수 있습니다. 단, Provider+ProviderId 조합은 시스템 전체에서 고유하여야 합니다.")
    List<MemberOAuthInfo> addMemberOAuth(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody AddOAuthRequest request) {

        OAuthVerificationClaims oAuthClaims = jwtTokenProvider.parseOAuthVerificationToken(
            request.oAuthVerificationToken());

        oAuthAuthenticationUseCase.linkOAuth(
            LinkOAuthCommand.builder()
                .memberId(memberPrincipal.getMemberId())
                .provider(oAuthClaims.provider())
                .providerId(oAuthClaims.providerId())
                .build()
        );

        return oAuthListUseCase.getOAuthList(memberPrincipal.getMemberId());
    }

    @DeleteMapping("{memberOAuthId}")
    @Operation(summary = "로그인용 OAuth 수단 제거",
        description = """
            현재는 memberOAuthId로 식별해서 제거 처리를 진행하나, 추후 OAuth측에 다시 로그인해서 제거하는 방식으로 변경될 수 있습니다.
            """)
    List<MemberOAuthInfo> deleteMemberOAuth(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long memberOAuthId
    ) {
        oAuthAuthenticationUseCase.unlinkOAuth(
            UnlinkOAuthCommand.builder()
                .memberId(memberPrincipal.getMemberId())
                .memberOAuthId(memberOAuthId)
                .build()
        );

        return oAuthListUseCase.getOAuthList(memberPrincipal.getMemberId());
    }

    @GetMapping("me")
    @Operation(summary = "현재 회원 계정과 연동된 OAuth 정보 조회")
    List<MemberOAuthInfo> getMyOAuthInfos(@CurrentMember MemberPrincipal memberPrincipal) {
        return oAuthListUseCase.getOAuthList(memberPrincipal.getMemberId());
    }
}
