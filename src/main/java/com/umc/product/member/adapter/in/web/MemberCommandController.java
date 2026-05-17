package com.umc.product.member.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.OAuthVerificationClaims;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.adapter.in.web.dto.request.DeleteMemberRequest;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberInfoRequest;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberProfileRequest;
import com.umc.product.member.adapter.in.web.dto.request.EmailRegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.request.OAuthRegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.RegisterResponse;
import com.umc.product.member.application.port.in.command.ManageMemberProfileUseCase;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.notification.application.port.in.annotation.WebhookAlarm;
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
@Tag(name = "Member | 회원 Command", description = "회원가입, 정보 수정, 탈퇴 등")
public class MemberCommandController {

    private final MemberInfoResponseAssembler assembler;

    private final JwtTokenProvider jwtTokenProvider;

    private final ManageMemberUseCase manageMemberUseCase;
    private final ManageMemberProfileUseCase manageMemberProfileUseCase;

    private final RegisterOAuthMemberUseCase registerOAuthMemberUseCase;
    private final RegisterEmailMemberUseCase registerEmailMemberUseCase;


    @Public
    @Operation(summary = "[REGISTER-001] OAuth 회원가입",
        description = """
            ### ⚠️ `register/oauth` 엔드포인트를 사용해주셔야 합니다. 기존 엔트포인트는 `v2.0.0`이 Production에 배포될 때 제거될 예정입니다.


            OAuth2 로그인을 통해서 oAuthVerificationToken 및 Email 인증을 통한 emailVerificationToken을 발급받은 후,
            해당 토큰들을 첨부해서 회원가입을 진행해주세요.
            """)
    @PostMapping({"/register", "/register/oauth"})
    @WebhookAlarm(
        title = "'새로운 회원이 가입했어요!'",
        content = "'회원 ID: ' + #result.memberId + '\n닉네임/이름: ' + #request.nickname + '/' + #request.name + '\n학교: ' + #request.schoolId"
    )
    RegisterResponse registerMemberByOAuth(@RequestBody OAuthRegisterMemberRequest request) {
        OAuthVerificationClaims claims = jwtTokenProvider.parseOAuthVerificationToken(request.oAuthVerificationToken());
        String email = jwtTokenProvider.parseEmailVerificationToken(request.emailVerificationToken());

        OAuthRegisterMemberCommand command = OAuthRegisterMemberCommand
            .builder()
            .provider(claims.provider())
            .providerId(claims.providerId())
            .name(request.name())
            .nickname(request.nickname())
            .email(email)
            .schoolId(request.schoolId())
            .profileImageId(request.profileImageId())
            .termConsents(request.termsAgreements().stream().map(TermConsents::fromRequest).toList())
            .appleRefreshToken(request.appleRefreshToken())
            .appleClientId(request.appleClientId())
            .build();

        Long createdMemberId = registerOAuthMemberUseCase.register(command);

        String accessToken = jwtTokenProvider.createAccessToken(createdMemberId, null);
        String refreshToken = jwtTokenProvider.createRefreshToken(createdMemberId);

        return RegisterResponse.of(createdMemberId, accessToken, refreshToken);
    }

    @Operation(summary = "[REGISTER-003] 이메일/PW 이용 회원가입",
        description = """
            ADR-017 에 따라 도입된 이메일 기반 회원가입 엔드포인트입니다.

            로그인 식별자를 별도로 받지 않으며, `emailVerificationToken` 으로 검증된 이메일이
            그대로 로그인 식별자로 사용됩니다.
            """)
    @PostMapping("/register/email")
    @Public
    @WebhookAlarm(
        title = "'새로운 회원이 가입했어요!'",
        content = "'회원 ID: ' + #result.memberId + '\n닉네임/이름: ' + #request.nickname + '/' + #request.name + '\n학교: ' + #request.schoolId"
    )
    RegisterResponse registerMemberByEmail(@RequestBody EmailRegisterMemberRequest request) {
        String email = jwtTokenProvider.parseEmailVerificationToken(request.emailVerificationToken());

        Long createdMemberId = registerEmailMemberUseCase.register(request.toCommand(email));

        String accessToken = jwtTokenProvider.createAccessToken(createdMemberId, null);
        String refreshToken = jwtTokenProvider.createRefreshToken(createdMemberId);

        return RegisterResponse.of(createdMemberId, accessToken, refreshToken);
    }

    @Operation(summary = "[MEMBER-001] 내 회원 정보 수정")
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

    @Operation(summary = "[MEMBER-002] 내 회원 프로필 링크 수정")
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

    @DeleteMapping
    @Operation(summary = "[MEMBER-003] 회원 탈퇴",
        description = "Google/Kakao OAuth 연동이 있는 경우 해당 Provider의 Access Token을 함께 전달하면 Provider측 연결도 해제됩니다.")
    @WebhookAlarm(
        title = "'회원이 탈퇴하였습니다'",
        content = "'회원 ID: ' + #memberPrincipal.getMemberId() + '\n닉네임/이름: ' + #result.nickname() + '/' + #result.name() + '\n학교: ' + #result.schoolName()"
    )
    public MemberInfoResponse deleteMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody(required = false) DeleteMemberRequest request
    ) {
        return deleteMemberById(memberPrincipal.getMemberId(), request);
    }

    @Operation(summary = "[MEMBER-004] 관리자 권한으로 회원 게정 삭제 (Hard Delete)", description = "총괄단 권한이 필요합니다. (적용 전)")
    @DeleteMapping("{memberId}")
    @CheckAccess(
        resourceType = ResourceType.MEMBER,
        resourceId = "#memberId",
        permission = PermissionType.DELETE
    )
    @WebhookAlarm(
        title = "'관리자가 계정을 삭제하였습니다.'",
        content = "'회원 ID: ' + #memberId + '\n닉네임/이름: ' + #result.nickname() + '/' + #result.name() + '\n학교: ' + #result.schoolName()"
    )
    public MemberInfoResponse deleteMember(@PathVariable Long memberId) {
        return deleteMemberById(memberId, null);
    }

    private MemberInfoResponse deleteMemberById(Long memberId, DeleteMemberRequest request) {
        MemberInfoResponse deletedMemberInfoResponse = assembler.fromMemberId(memberId);

        manageMemberUseCase.deleteMember(
            DeleteMemberCommand
                .builder()
                .memberId(memberId)
                .googleAccessToken(request != null ? request.googleAccessToken() : null)
                .kakaoAccessToken(request != null ? request.kakaoAccessToken() : null)
                .build()
        );

        return deletedMemberInfoResponse;
    }
}
