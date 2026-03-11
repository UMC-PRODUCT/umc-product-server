package com.umc.product.member.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.adapter.in.web.dto.request.SearchMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.adapter.in.web.dto.response.SearchMemberResponse;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final MemberInfoResponseAssembler assembler;
    private final SearchMemberUseCase searchMemberUseCase;

    @Operation(summary = "memberId로 회원 정보 조회")
    @GetMapping("profile/{memberId}")
    @CheckAccess(
        resourceType = ResourceType.MEMBER,
        permission = PermissionType.READ
    )
    MemberInfoResponse getMemberProfile(
        @PathVariable Long memberId
    ) {
        return assembler.fromMemberIdToPublic(memberId);
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("me")
    MemberInfoResponse getMyProfile(@CurrentMember MemberPrincipal memberPrincipal) {
        return assembler.fromMemberId(memberPrincipal.getMemberId());
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

}
