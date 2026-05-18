package com.umc.product.global.security;

import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class MemberPrincipal {

    private final Long memberId;

    @Builder
    public MemberPrincipal(Long memberId) {
        this.memberId = memberId;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "MemberPrincipal{" +
                "memberId=" + memberId +
                '}';
    }
}
