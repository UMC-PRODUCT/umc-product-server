package com.umc.product.test.application.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * test 도메인 시딩 API 설정. ADR-017 참조.
 * <p>
 * 활성화는 두 조건을 모두 만족해야 한다.
 * <ol>
 *   <li>spring.profiles.active 가 prod 가 아닐 것</li>
 *   <li>app.seed.enabled = true</li>
 * </ol>
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
    boolean enabled,
    long skipIfMemberCountGreaterThan,
    int idPwMemberCount,
    int oauthMemberCount,
    String emailDomain,
    String defaultPassword
) {
}
