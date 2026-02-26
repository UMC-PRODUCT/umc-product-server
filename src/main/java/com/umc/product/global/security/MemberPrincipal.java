package com.umc.product.global.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@Slf4j
public class MemberPrincipal implements OAuth2User {

    private final Long memberId;

    @Builder
    public MemberPrincipal(Long memberId) {
        this.memberId = memberId;
    }

    // OAuth2User 메서드 구현

    @Override
    public Map<String, Object> getAttributes() {
//        log.warn("OAuth2User의 getAttributes()가 호출되었습니다.");
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        log.warn("OAuth2User의 getAuthorities()가 호출되었습니다.");
        return Collections.emptyList();
    }

    @Override
    public String getName() {
//        log.warn("OAuth2User의 getName()가 호출되었습니다.");
        return String.valueOf(memberId);
    }

    // toString 메서드 오버라이드

    @Override
    public String toString() {
        return "MemberPrincipal{" +
                "memberId=" + memberId +
                '}';
    }
}
