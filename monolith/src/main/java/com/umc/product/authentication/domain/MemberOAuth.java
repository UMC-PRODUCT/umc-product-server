package com.umc.product.authentication.domain;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_oauth", uniqueConstraints = {
    @UniqueConstraint(name = "uk_member_oauth_provider_provider_id",
        columnNames = {"oauth_provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // ID 참조만

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 512)
    private String providerId;

    @Column(name = "apple_refresh_token", length = 512)
    private String appleRefreshToken;

    /**
     * Apple Sign-In 시 사용된 client_id (Bundle ID 또는 Services ID).
     * <p>
     * Apple은 플랫폼별로 서로 다른 client_id를 사용하므로 revoke 시 발급 시점과 동일한 값이 필요해 보관한다.
     */
    @Column(name = "apple_client_id", length = 255)
    private String appleClientId;

    @Builder
    private MemberOAuth(Long memberId, OAuthProvider provider, String providerId, String appleRefreshToken,
                        String appleClientId) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
        this.appleRefreshToken = appleRefreshToken;
        this.appleClientId = appleClientId;
    }

    /**
     * Apple 재로그인 시 refresh token과 client_id를 함께 갱신한다. 사용자가 다른 플랫폼(Web ↔ iOS)에서 다시 로그인하면 client_id가 바뀔 수 있다.
     */
    public void updateAppleCredentials(String refreshToken, String clientId) {
        this.appleRefreshToken = refreshToken;
        this.appleClientId = clientId;
    }

    /**
     * Member ID가 일치하는지 검증하는 도메인 로직
     */
    public boolean validateMember(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void throwIfNotValidMember(Long memberId) {
        if (!validateMember(memberId)) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.NOT_VALID_MEMBER);
        }
    }
}
