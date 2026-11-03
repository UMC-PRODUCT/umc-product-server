package com.umc.product.member.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * 명함 수정 화면에 필요한 정보입니다.
 * <p>
 * 본인만 보는 화면이므로 비공개 항목도 공개 여부와 함께 담습니다. 연락처·링크는 명함 수정 요청과 같은 모양입니다.
 *
 * @param activity       활동 이력 중 가장 최신 기수의 기수·파트. 활동 이력이 없으면 null
 * @param oauthProviders 연동된 소셜 로그인 수단
 * @param contact        연락처. 입력하지 않았으면 email이 null
 * @param links          링크 타입마다 하나씩, 항상 모든 타입을 담습니다. 입력하지 않은 링크는 link가 null
 */
public record BusinessCardEditInfo(
    Long memberId,
    String name,
    String schoolName,
    String profileImageLink,
    BusinessCardInfo.Activity activity,
    List<OAuthProvider> oauthProviders,
    BusinessCardInfo.Contact contact,
    List<BusinessCardInfo.Link> links
) {
}
