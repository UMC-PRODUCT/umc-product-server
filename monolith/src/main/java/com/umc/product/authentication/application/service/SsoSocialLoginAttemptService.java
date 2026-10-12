package com.umc.product.authentication.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authentication.application.port.in.command.CleanUpSsoSocialLoginAttemptsUseCase;
import com.umc.product.authentication.application.port.out.SsoSocialLoginAttemptPort;
import com.umc.product.authentication.domain.SsoSocialLoginAttempt;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SsoSocialLoginAttemptService implements CleanUpSsoSocialLoginAttemptsUseCase {
    private final SsoSocialLoginAttemptPort attempts;
    private final SecureTokenGenerator tokens;
    private final Clock clock;

    /**
     * provider state로 시도를 찾고, 제공자·브라우저·만료·사용 여부를 확인하여 한 번 소비한다.
     * <p>
     * 별도 Spring Bean의 REQUIRES_NEW이므로 이 메서드가 반환될 때 소비 상태가 커밋된다.
     * 이후 소셜 서버 장애나 회원 처리 실패가 발생해도 소비가 되돌아가지 않아 callback 재사용을 막는다.
     * DB 쓰기 잠금도 이 트랜잭션이 끝날 때 해제하므로 외부 네트워크 응답 동안 잠금을 유지하지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SsoSocialLoginAttempt consume(OAuthProvider provider, String state, String binding) {
        // 서버가 생성한 32바이트 난수의 Base64URL 형식이다. Mac에서 받은 원래 state와는 다르다.
        if (state == null || !state.matches("[A-Za-z0-9_-]{43}")
            || binding == null || !binding.matches("[A-Za-z0-9_-]{43}")) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_SOCIAL_ATTEMPT);
        }
        // DB에는 원문 대신 SHA-256 해시가 저장되어 있으므로 같은 방식으로 해시하여 조회한다.
        // 쓰기 잠금을 얻은 뒤 consume하므로 동시 callback도 같은 시도를 두 번 소비할 수 없다.
        SsoSocialLoginAttempt attempt = attempts.findByStateHashForUpdate(tokens.sha256Hex(state))
            .orElseThrow(() -> new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_SOCIAL_ATTEMPT));
        attempt.consume(provider, tokens.sha256Hex(binding), Instant.now(clock));
        return attempt;
    }

    @Override
    @Transactional
    public int cleanUp() {
        // 정리는 저장 공간 회수용이다. 정리 전에 만료 시도가 남아 있어도 consume에서 즉시 거절한다.
        return attempts.deleteExpired(Instant.now(clock));
    }
}
