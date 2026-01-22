package com.umc.product.member.adapter.in.web;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.OAuthVerificationClaims;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberInfoRequest;
import com.umc.product.member.adapter.in.web.dto.request.RegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.RegisterResponse;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.MEMBER)
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ManageMemberUseCase manageMemberUseCase;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;

    // 로그인은 OAuth를 통해서만 진행됨!!
    @Public
    @Operation(summary = "회원가입",
            description = """
                    OAuth2 로그인을 통해서 oAuthVerificationToken을 발급받은 후,
                    해당 토큰을 첨부해서 회원가입을 진행해주세요.
                    
                    해당 토큰은 사전에 인증된 OAuth2 Provider와 ProviderId를 인증해줍니다.
                    """)
    @PostMapping("register")
    RegisterResponse registerMember(@RequestBody RegisterMemberRequest request) {
        // TODO: oAuthVerificationToken은 authentication domain에서 port 가져와서 처리함

        OAuthVerificationClaims claims = jwtTokenProvider.parseOAuthVerificationToken(request.oAuthVerificationToken());
        String email = jwtTokenProvider.parseEmailVerificationToken(request.emailVerificationToken());

        RegisterMemberCommand command = RegisterMemberCommand
                .builder()
                .provider(claims.provider())
                .providerId(claims.providerId())
                .name(request.name())
                .nickname(request.nickname())
                .email(email)
                .schoolId(request.schoolId())
                .profileImageId(request.profileImageId())
                .termConsents(request.termsAgreements().stream().map(TermConsents::fromRequest).toList())
                .build();

        // TODO: 약관 동의 처리 해야함

        Long createdMemberId = manageMemberUseCase.registerMember(command);

        return RegisterResponse.builder()
                .memberId(createdMemberId)
                .build();
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("me")
    MemberInfoResponse getMyProfile(@CurrentMember MemberPrincipal memberPrincipal) {
        throw new NotImplementedException();
    }

    @Operation(summary = "memberId로 회원 정보 조회")
    @GetMapping("profile/{memberId}")
    MemberInfoResponse getMemberProfile(
            @PathVariable String memberId
    ) {
        throw new NotImplementedException();
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping
    MemberInfoResponse editMemberInfo(
            @CurrentMember MemberPrincipal memberPrincipal,
            @RequestBody EditMemberInfoRequest request
    ) {
        throw new NotImplementedException();
    }

}
