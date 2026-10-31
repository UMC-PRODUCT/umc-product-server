package com.umc.product.member.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.adapter.in.web.dto.response.BusinessCardResponse.ActivityResponse;
import com.umc.product.member.adapter.in.web.dto.response.BusinessCardResponse.ContactResponse;
import com.umc.product.member.adapter.in.web.dto.response.BusinessCardResponse.LinkResponse;
import com.umc.product.member.application.port.in.query.dto.BusinessCardEditInfo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 명함 수정 화면 응답입니다. 연락처·링크는 명함 수정 요청과 같은 모양이며, 비공개 항목도 공개 여부와 함께 포함합니다.
 */
public record BusinessCardEditResponse(
    Long memberId,
    String name,
    String schoolName,
    String profileImageLink,
    @Schema(description = "활동 이력 중 가장 최신 기수의 기수·파트. 활동 이력이 없으면 null", nullable = true)
    ActivityResponse activity,
    @Schema(description = "연동된 소셜 로그인 수단")
    List<OAuthProvider> oauthProviders,
    @Schema(description = "연락처. 입력하지 않았으면 email이 null")
    ContactResponse contact,
    @Schema(description = "링크 타입마다 하나씩 항상 5개 (LINKEDIN, INSTAGRAM, GITHUB, BLOG, PERSONAL 순). 입력하지 않았으면 link가 null")
    List<LinkResponse> links
) {

    public static BusinessCardEditResponse from(BusinessCardEditInfo info) {
        return new BusinessCardEditResponse(
            info.memberId(),
            info.name(),
            info.schoolName(),
            info.profileImageLink(),
            ActivityResponse.from(info.activity()),
            info.oauthProviders(),
            ContactResponse.from(info.contact()),
            info.links().stream().map(LinkResponse::from).toList()
        );
    }
}
