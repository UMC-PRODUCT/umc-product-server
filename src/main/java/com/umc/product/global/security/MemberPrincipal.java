package com.umc.product.global.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class MemberPrincipal implements OAuth2User {

    private final Long memberId;
    private final String email;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public MemberPrincipal(Long memberId, String email,
                           Map<String, Object> attributes,
                           String nameAttributeKey) {
        this.memberId = memberId;
        this.email = email;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    // JWT 인증용 생성자 (OAuth 없이 사용)
    public MemberPrincipal(Long memberId, String email) {
        this(memberId, email, Collections.emptyMap(), "id");
    }

    // OAuth2User 메서드 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        if (attributes.isEmpty()) {
            return String.valueOf(memberId);
        }
        return String.valueOf(attributes.get(nameAttributeKey));
    }
}
