package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.authentication.application.port.in.command.ManageSsoSocialLoginUseCase;
import com.umc.product.authentication.application.port.in.command.dto.CompleteSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.out.SsoSocialLoginAttemptPort;
import com.umc.product.authentication.application.port.out.SsoSocialProviderPort;
import com.umc.product.authentication.domain.SsoSocialLoginAttempt;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("소셜 로그인 시도 트랜잭션·동시성")
class SsoSocialLoginAttemptIntegrationTest extends IntegrationTestSupport {
    @Autowired SsoSocialLoginAttemptPort attempts;
    @Autowired SsoSocialLoginAttemptService consumer;
    @Autowired SecureTokenGenerator tokens;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ManageSsoSocialLoginUseCase social;
    @MockitoBean
    SsoSocialProviderPort provider;

    @Test
    @DisplayName("동시 완료는 제공자를 한 번만 호출하며 외부 실패 후에도 재사용을 거절한다")
    void 동시_완료와_외부_실패() throws Exception {
        String state = tokens.generateOpaqueToken();
        String binding = tokens.generateOpaqueToken();
        save(state, binding);
        BDDMockito.given(provider.exchange(ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .willThrow(new AuthenticationDomainException(
                AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE));
        var command = new CompleteSsoSocialLoginCommand(
            OAuthProvider.GOOGLE, "provider-code", state, null, binding);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> action = () -> {
                start.await();
                try {
                    return social.complete(command).contains("error=temporarily_unavailable");
                } catch (AuthenticationDomainException e) {
                    return false;
                }
            };
            var first = executor.submit(action);
            var second = executor.submit(action);
            start.countDown();
            assertThat(java.util.List.of(first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                second.get(10, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThatThrownBy(() -> social.complete(command)).isInstanceOf(AuthenticationDomainException.class);
        Mockito.verify(provider, Mockito.times(1)).exchange(
            ArgumentMatchers.any(), ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("동시 callback은 DB 잠금으로 한 번만 소비한다")
    void 동시_callback() throws Exception {
        String state = tokens.generateOpaqueToken();
        String binding = tokens.generateOpaqueToken();
        save(state, binding);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> action = () -> {
                start.await();
                try {
                    consumer.consume(OAuthProvider.GOOGLE, state, binding);
                    return true;
                } catch (AuthenticationDomainException e) {
                    return false;
                }
            };
            var first = executor.submit(action);
            var second = executor.submit(action);
            start.countDown();
            assertThat(java.util.List.of(first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                second.get(10, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
    }

    @Test
    @DisplayName("후속 처리 트랜잭션 실패로 callback 소비를 되돌릴 수 없다")
    void 후속_실패에도_소비_유지() {
        String state = tokens.generateOpaqueToken();
        String binding = tokens.generateOpaqueToken();
        save(state, binding);
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            consumer.consume(OAuthProvider.GOOGLE, state, binding);
            throw new IllegalStateException("후속 처리 실패");
        })).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> consumer.consume(OAuthProvider.GOOGLE, state, binding))
            .isInstanceOf(AuthenticationDomainException.class);
    }

    private void save(String state, String binding) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> attempts.save(
            SsoSocialLoginAttempt.create(OAuthProvider.GOOGLE, tokens.sha256Hex(state), tokens.sha256Hex(binding),
                "macos-app", "umc-macos://auth/callback", "mac-state", "A".repeat(43),
                "https://api.example.com/api/v1/auth/sso/social/google/callback", "nonce", Instant.now().plusSeconds(600))));
    }
}
