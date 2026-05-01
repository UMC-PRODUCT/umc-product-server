package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityConfig 의 DelegatingPasswordEncoder 구성을 그대로 재현하여
 * Argon2 기본 / 다중 알고리즘 검증 / upgradeEncoding 동작을 확인한다.
 * <p>
 * Spring Context 를 띄우지 않는 순수 단위 테스트로, 실제 운영 인코더 동작을 빠르게 검증한다.
 */
class PasswordEncoderConfigTest {

    private static final String RAW_PASSWORD = "Strong-Pw-2026";

    private DelegatingPasswordEncoder encoder;
    private BCryptPasswordEncoder rawBcrypt;

    @BeforeEach
    void setUp() {
        // SecurityConfig#passwordEncoder() 와 동일한 구성
        final String defaultEncodingId = "argon2";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(defaultEncodingId, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        rawBcrypt = new BCryptPasswordEncoder();
        encoders.put("bcrypt", rawBcrypt);
        encoder = new DelegatingPasswordEncoder(defaultEncodingId, encoders);
    }

    @Test
    @DisplayName("기본 인코딩은 {argon2} prefix 가 붙는다")
    void encode_시_argon2_prefix가_붙는다() {
        // when
        String encoded = encoder.encode(RAW_PASSWORD);

        // then
        assertThat(encoded).startsWith("{argon2}");
    }

    @Test
    @DisplayName("argon2 로 인코딩된 해시는 같은 평문에 대해 matches=true 를 반환한다")
    void argon2_해시_검증_성공() {
        // given
        String encoded = encoder.encode(RAW_PASSWORD);

        // when
        boolean matches = encoder.matches(RAW_PASSWORD, encoded);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("argon2 해시에 대해 다른 평문은 matches=false 를 반환한다")
    void argon2_해시_다른_평문이면_불일치() {
        // given
        String encoded = encoder.encode(RAW_PASSWORD);

        // when & then
        assertThat(encoder.matches("Wrong-Pw-2026", encoded)).isFalse();
    }

    @Test
    @DisplayName("bcrypt prefix 가 붙은 기존 해시도 검증할 수 있다")
    void bcrypt_prefix_해시_검증_성공() {
        // given: 외부에서 bcrypt 로 인코딩된 해시에 prefix 를 붙여 마이그레이션한 케이스 모사
        String bcryptEncoded = "{bcrypt}" + rawBcrypt.encode(RAW_PASSWORD);

        // when
        boolean matches = encoder.matches(RAW_PASSWORD, bcryptEncoded);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("기본(argon2) 으로 인코딩된 해시는 upgradeEncoding=false")
    void argon2_해시는_upgrade_불필요() {
        // given
        String encoded = encoder.encode(RAW_PASSWORD);

        // when
        boolean needsUpgrade = encoder.upgradeEncoding(encoded);

        // then
        assertThat(needsUpgrade).isFalse();
    }

    @Test
    @DisplayName("기본이 아닌 알고리즘(bcrypt) 의 해시는 upgradeEncoding=true 로 점진적 rehash 대상이 된다")
    void bcrypt_해시는_upgrade_필요() {
        // given
        String bcryptEncoded = "{bcrypt}" + rawBcrypt.encode(RAW_PASSWORD);

        // when
        boolean needsUpgrade = encoder.upgradeEncoding(bcryptEncoded);

        // then
        assertThat(needsUpgrade).isTrue();
    }
}
