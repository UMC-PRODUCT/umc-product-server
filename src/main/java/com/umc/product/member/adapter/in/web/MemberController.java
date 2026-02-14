package com.umc.product.member.adapter.in.web;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.in.query.GetOAuthListUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.OAuthVerificationClaims;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.adapter.in.web.dto.request.EditMemberInfoRequest;
import com.umc.product.member.adapter.in.web.dto.request.RegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.request.SearchMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.RegisterResponse;
import com.umc.product.member.adapter.in.web.dto.response.SearchMemberResponse;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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
@Tag(name = "Member | 회원", description = "")
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ManageMemberUseCase manageMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final SearchMemberUseCase searchMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final GetOAuthListUseCase getOAuthListUseCase;


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

    @Operation(summary = "memberId로 회원 정보 조회")
    @GetMapping("profile/{memberId}")
    MemberInfoResponse getMemberProfile(
        @PathVariable Long memberId
    ) {
        // TODO: 총괄단만 가능하도록 권한을 부여하여야 함.
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        List<ChallengerInfoResponse> challengerInfoResponses =
            getChallengerUseCase.getMemberChallengerList(memberId)
                .stream()
                .map(info -> {
                    GisuInfo gisuInfo = getGisuUseCase.getById(info.gisuId());
                    return ChallengerInfoResponse.from(info, memberInfo, gisuInfo);
                })
                .toList();

        return MemberInfoResponse.from(memberInfo, challengerInfoResponses);
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("me")
    MemberInfoResponse getMyProfile(@CurrentMember MemberPrincipal memberPrincipal) {
        Long memberId = memberPrincipal.getMemberId();
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        // 회원이 챌린저로 활동한 기록 채워넣기
        List<ChallengerInfoResponse> challengerInfoResponses =
            getChallengerUseCase.getMemberChallengerList(memberId)
                .stream()
                .map(info -> {
                    GisuInfo gisuInfo = getGisuUseCase.getById(info.gisuId());
                    return ChallengerInfoResponse.from(info, memberInfo, gisuInfo);
                })
                .toList();

        return MemberInfoResponse.from(memberInfo, challengerInfoResponses);
    }

    @Operation(summary = "회원 검색", description = "이름, 닉네임, 이메일, 학교명으로 검색, 기수/파트/지부/학교별 필터링")
    @GetMapping("search")
    SearchMemberResponse searchMembers(
        @ParameterObject Pageable pageable,
        @ParameterObject SearchMemberRequest searchRequest
    ) {
        return SearchMemberResponse.from(
            searchMemberUseCase.search(searchRequest.toQuery(), pageable)
        );
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

        MemberProfileInfo info = getMemberUseCase.getProfile(memberPrincipal.getMemberId());
        return MemberInfoResponse.from(info);
    }

    public MemberInfo deleteMember(@CurrentMember MemberPrincipal memberPrincipal) {
        Long memberId = memberPrincipal.getMemberId();

        MemberInfo deletedMemberInfo = getMemberUseCase.getById(memberId);

        List<MemberOAuthInfo> linkedOAuths = getOAuthListUseCase.getOAuthList(memberId);

        // TODO: N+1 문제 해결 필요
        for (MemberOAuthInfo oAuthInfo : linkedOAuths) {
            oAuthAuthenticationUseCase.unlinkOAuth(
                UnlinkOAuthCommand.builder()
                    .memberId(memberId)
                    .memberOAuthId(oAuthInfo.memberOAuthId())
                    .build()
            );
        }

        manageMemberUseCase.deleteMember(
            DeleteMemberCommand
                .builder()
                .memberId(memberId)
                .build()
        );

        // TODO: 회원 탈퇴 후에도 다른 도메인에서 정보를 조회했을 때 null-safe하게 동작할 수 있도록 변경 필요

        return deletedMemberInfo;
    }

}
