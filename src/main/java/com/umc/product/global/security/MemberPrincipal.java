package com.umc.product.global.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.umc.product.common.domain.enums.ClientType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberPrincipal {

    private final Long memberId;

    // AT claim 으로 전달된 클라이언트 플랫폼. 도입 이전 토큰 / claim 누락 토큰은 null.
    // 통계/로그 컨텍스트용 메타데이터이며, 인가 결정에는 영향을 주지 않는다.
    private final ClientType clientType;

    private final boolean requiredTermsAgreed;
    private final List<Long> agreedRequiredTermIds;

    @Builder
    public MemberPrincipal(
        Long memberId,
        ClientType clientType,
        Boolean requiredTermsAgreed,
        List<Long> agreedRequiredTermIds
    ) {
        this.memberId = memberId;
        this.clientType = clientType;
        this.requiredTermsAgreed = requiredTermsAgreed == null || requiredTermsAgreed;
        this.agreedRequiredTermIds = agreedRequiredTermIds == null
            ? List.of()
            : List.copyOf(agreedRequiredTermIds);
    }

    public MemberPrincipal(Long memberId, ClientType clientType) {
        this(memberId, clientType, true, List.of());
    }

    public MemberPrincipal(Long memberId) {
        this(memberId, null);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "MemberPrincipal{"
            + "memberId=" + memberId
            + ", clientType=" + clientType
            + ", requiredTermsAgreed=" + requiredTermsAgreed
            + ", agreedRequiredTermIds=" + agreedRequiredTermIds
            + '}';
    }
}
