package com.umc.product.global.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentMemberProvider {

    public MemberPrincipal getRequiredCurrentMember() {
        MemberPrincipal memberPrincipal = getNullableCurrentMember();
        if (memberPrincipal == null) {
            throw new AccessDeniedException("로그인이 필요해요. 로그인 후 다시 시도해주세요.");
        }
        return memberPrincipal;
    }

    public Long getRequiredCurrentMemberId() {
        return getRequiredCurrentMember().getMemberId();
    }

    public MemberPrincipal getNullableCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof MemberPrincipal principal) {
            return principal;
        }
        return null;
    }

    /**
     * 인증 정보가 아예 없는 요청인지 확인한다. 데모데이 게스트처럼 회원이 아닌 인증은 익명으로 보지 않는다.
     */
    public boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    public Long getNullableCurrentMemberId() {
        MemberPrincipal memberPrincipal = getNullableCurrentMember();
        return memberPrincipal == null ? null : memberPrincipal.getMemberId();
    }
}
