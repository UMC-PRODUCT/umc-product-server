package com.umc.product.member.adapter.in.web;

import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.OAuthVerificationClaims;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberInfoRequest;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberProfileRequest;
import com.umc.product.member.adapter.in.web.dto.request.RegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.RegisterResponse;
import com.umc.product.member.application.port.in.command.ManageMemberProfileUseCase;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member | 회원 Command", description = "")
public class MemberCommandController {

    private final MemberInfoResponseAssembler assembler;

    private final JwtTokenProvider jwtTokenProvider;
    private final ManageMemberUseCase manageMemberUseCase;
    private final ManageMemberProfileUseCase manageMemberProfileUseCase;


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

        Long createdMemberId = manageMemberUseCase.registerMember(command);

        String accessToken = jwtTokenProvider.createAccessToken(createdMemberId, null);
        String refreshToken = jwtTokenProvider.createRefreshToken(createdMemberId);

        return RegisterResponse.builder()
            .memberId(createdMemberId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping
    MemberInfoResponse editMemberInfo(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody EditMemberInfoRequest request
    ) {
        manageMemberUseCase.updateMember(UpdateMemberCommand.forProfileUpdate(
            memberPrincipal.getMemberId(),
            request.profileImageId())
        );

        return assembler.fromMemberId(memberPrincipal.getMemberId());
    }

    @Operation(summary = "회원 프로필 링크 수정")
    @PatchMapping("/profile/links")
    MemberInfoResponse editMemberProfile(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody EditMemberProfileRequest request
    ) {
        manageMemberProfileUseCase.upsert(
            request.toCommand(memberPrincipal.getMemberId())
        );

        return assembler.fromMemberId(memberPrincipal.getMemberId());
    }

    // TODO: 총괄이 임의로 계정을 삭제시키려면 memberId로 삭제하는 API도 필요할 것 같음

    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    public MemberInfoResponse deleteMember(@CurrentMember MemberPrincipal memberPrincipal) {
        return deleteMemberById(memberPrincipal.getMemberId());
    }

    @Operation(summary = "관리자 권한으로 회원 게정 삭제 (Hard Delete)", description = "SUPER_ADMIN 권한이 필요합니다. (적용 전)")
    @DeleteMapping("{memberId}")
    public MemberInfoResponse deleteMember(@PathVariable Long memberId) {
        // TODO: SUPER_ADMIN 권한 필요

        return deleteMemberById(memberId);
    }

    private MemberInfoResponse deleteMemberById(Long memberId) {
        MemberInfoResponse deletedMemberInfoResponse = assembler.fromMemberId(memberId);

        manageMemberUseCase.deleteMember(
            DeleteMemberCommand
                .builder()
                .memberId(memberId)
                .build()
        );

        // TODO: 회원 탈퇴 후에도 다른 도메인에서 정보를 조회했을 때 null-safe하게 동작할 수 있도록 변경 필요

        return deletedMemberInfoResponse;
    }
}
