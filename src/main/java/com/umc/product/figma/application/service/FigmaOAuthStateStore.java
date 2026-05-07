package com.umc.product.figma.application.service;

import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Figma OAuth Authorization Code Flow 의 state 보관소.
 * <p>
 * 보안 책임:
 * <ul>
 *   <li>state = 256-bit {@link SecureRandom} URL-safe 문자열 (추측 불가)</li>
 *   <li>발급 시 (memberId, expiresAt) 를 함께 저장 — 인증된 /start 호출 없이는 위조 불가</li>
 *   <li>{@link #consume(String)} 가 원자적으로 lookup + 제거 → replay 차단</li>
 *   <li>10분 TTL — 만료 entry 는 거부, 신규 발급 시 lazy cleanup</li>
 * </ul>
 * <p>
 * 한계:
 * <ul>
 *   <li>in-memory: 다중 인스턴스 환경에서는 sticky session / Redis 로 교체 필요.
 *       ADR-003 의 단일 위임자 / 저빈도 호출 전제 하에서는 충분.</li>
 *   <li>서버 재시작 시 발급 중이던 state 는 무효 — 운영진이 /start 부터 재시도.</li>
 * </ul>
 */
@Component
public class FigmaOAuthStateStore {

    static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final int STATE_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, IssuedState> issued = new ConcurrentHashMap<>();

    /**
     * 인증된 운영진의 memberId 와 함께 신규 state 를 발급한다.
     */
    public String issue(Long memberId) {
        cleanupExpired();

        byte[] bytes = new byte[STATE_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = Instant.now().plus(STATE_TTL);
        issued.put(state, new IssuedState(memberId, expiresAt));
        return state;
    }

    /**
     * state 를 검증하면서 동시에 제거하고 묶여 있던 memberId 를 반환한다. 발급된 적 없거나 만료된 경우 {@link FigmaErrorCode#OAUTH_STATE_MISMATCH} 를
     * 던진다.
     */
    public Long consume(String state) {
        if (state == null) {
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_STATE_MISMATCH);
        }
        IssuedState entry = issued.remove(state);
        if (entry == null || entry.isExpired(Instant.now())) {
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_STATE_MISMATCH);
        }
        return entry.memberId();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        for (Iterator<Map.Entry<String, IssuedState>> it = issued.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue().isExpired(now)) {
                it.remove();
            }
        }
    }

    private record IssuedState(Long memberId, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return now.isAfter(expiresAt);
        }
    }
}
