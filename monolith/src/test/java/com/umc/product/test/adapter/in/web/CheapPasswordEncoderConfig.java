package com.umc.product.test.adapter.in.web;

import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * QA 시딩은 계정 수백 개의 비밀번호를 해시하므로, 시딩 통합 테스트는 bcrypt 강도를 최저로 낮춘다.
 */
@TestConfiguration
class CheapPasswordEncoderConfig {
    @Bean
    @Primary
    PasswordEncoder cheapPasswordEncoder() {
        return new DelegatingPasswordEncoder("bcrypt", Map.of("bcrypt", new BCryptPasswordEncoder(4)));
    }
}
