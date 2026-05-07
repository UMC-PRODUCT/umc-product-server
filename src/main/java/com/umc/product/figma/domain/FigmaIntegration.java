package com.umc.product.figma.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Figma OAuth 위임 통합 상태.
 * 운영진 1인이 위임한 refresh token / access token을 저장하고,
 * 서버가 access token을 자동 갱신하는 데 사용한다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_integration")
public class FigmaIntegration extends BaseEntity {

    private static final long ACCESS_TOKEN_RENEW_BUFFER_SECONDS = 60L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_member_id", nullable = false)
    private Long ownerMemberId;

    @Column(name = "refresh_token_enc", nullable = false, columnDefinition = "TEXT")
    private String refreshTokenEnc;

    @Column(name = "access_token_enc", columnDefinition = "TEXT")
    private String accessTokenEnc;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    @Column(nullable = false, length = 500)
    private String scope;

    public static FigmaIntegration of(
        Long ownerMemberId,
        String refreshTokenEnc,
        String accessTokenEnc,
        Instant accessTokenExpiresAt,
        String scope
    ) {
        return FigmaIntegration.builder()
            .ownerMemberId(ownerMemberId)
            .refreshTokenEnc(refreshTokenEnc)
            .accessTokenEnc(accessTokenEnc)
            .accessTokenExpiresAt(accessTokenExpiresAt)
            .scope(scope)
            .build();
    }

    /**
     * 같은 위임자가 재인증 했을 때 토큰을 모두 교체한다.
     */
    public void rotateTokens(String refreshTokenEnc, String accessTokenEnc, Instant accessTokenExpiresAt, String scope) {
        this.refreshTokenEnc = refreshTokenEnc;
        this.accessTokenEnc = accessTokenEnc;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.scope = scope;
    }

    /**
     * refresh로 새 access token을 발급받았을 때 호출.
     */
    public void rotateAccessToken(String accessTokenEnc, Instant accessTokenExpiresAt) {
        this.accessTokenEnc = accessTokenEnc;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    /**
     * 현재 access token이 곧 만료되거나 이미 만료된 상태인지.
     */
    public boolean isAccessTokenExpired(Instant now) {
        if (accessTokenEnc == null || accessTokenExpiresAt == null) {
            return true;
        }
        return now.plusSeconds(ACCESS_TOKEN_RENEW_BUFFER_SECONDS).isAfter(accessTokenExpiresAt);
    }
}
