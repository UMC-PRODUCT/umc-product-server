package com.umc.product.global.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * alpha 환경 더미 데이터 시딩 설정. ADR-007 참조.
 * <p>
 * 활성화는 두 조건을 모두 만족해야 한다.
 * <ol>
 *   <li>spring.profiles.active 가 alpha 를 포함</li>
 *   <li>app.seed.alpha.enabled = true</li>
 * </ol>
 */
@ConfigurationProperties(prefix = "app.seed.alpha")
public record AlphaSeedProperties(
    boolean enabled,
    long skipIfMemberCountGreaterThan,
    int idPwMemberCount,
    int oauthMemberCount,
    String emailDomain,
    String defaultPassword
) {
}
