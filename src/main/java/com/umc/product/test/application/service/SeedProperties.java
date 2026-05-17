package com.umc.product.test.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

/**
 * test 도메인 시딩 API 설정. ADR-017 참조.
 * <p>
 * 활성화는 두 조건을 모두 만족해야 한다.
 * <ol>
 *   <li>spring.profiles.active 가 prod 가 아닐 것</li>
 *   <li>app.seed.enabled = true</li>
 * </ol>
 * 두 조건이 모두 만족될 때만 빈으로 등록되므로 prod 환경에서는 인스턴스화되지 않는다.
 *
 * @param enabled                       시딩 API 활성화 플래그
 * @param skipIfMemberCountGreaterThan  현재 회원 수가 이 값보다 크면 force=false 호출은 스킵
 * @param emailDomain                   더미 회원 이메일 도메인 (예: alpha.umc.test)
 * @param defaultPassword               모든 더미 회원이 공통으로 사용하는 비밀번호
 */
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
    boolean enabled,
    long skipIfMemberCountGreaterThan,
    String emailDomain,
    String defaultPassword
) {
}
