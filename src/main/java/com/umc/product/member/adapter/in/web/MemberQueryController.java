package com.umc.product.member.adapter.in.web;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.query.GetOAuthListUseCase;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.request.SearchMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.SearchMemberResponse;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member | 회원 Query", description = "")
public class MemberQueryController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ManageMemberUseCase manageMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final SearchMemberUseCase searchMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final GetOAuthListUseCase getOAuthListUseCase;

    @Operation(summary = "memberId로 회원 정보 조회")
    @GetMapping("profile/{memberId}")
    MemberInfoResponse getMemberProfile(
        @PathVariable Long memberId
    ) {
        // TODO: 총괄단만 가능하도록 권한 제한 필요

        return getMemberInfoResponse(memberId);
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("me")
    MemberInfoResponse getMyProfile(@CurrentMember MemberPrincipal memberPrincipal) {
        return getMemberInfoResponse(memberPrincipal.getMemberId());
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

    private MemberInfoResponse getMemberInfoResponse(Long memberId) {
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
}
