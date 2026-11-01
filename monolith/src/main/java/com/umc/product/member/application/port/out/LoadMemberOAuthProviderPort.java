package com.umc.product.member.application.port.out;

import java.util.List;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * 회원과 연동된 소셜 로그인 수단을 조회합니다.
 * <p>
 * 소셜 연동 정보는 authentication 도메인 소유입니다. 해당 도메인의 계약이 바뀌면 구현 어댑터만 수정합니다.
 */
public interface LoadMemberOAuthProviderPort {

    /**
     * @return 연동된 소셜 로그인 수단. 중복 없이 enum 순서로 정렬. 연동이 없으면 빈 목록
     */
    List<OAuthProvider> listByMemberId(Long memberId);
}
