package com.umc.product.authentication.domain;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    /**
     * UUID, 인증 토큰임
     */
    @Column(name = "token", nullable = false, length = 100)
    private String token;

    /**
     * 6자리 숫자, 인증 코드
     */
    @Column(name = "code", nullable = false, length = 10)
    private String code;


    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verified_by", length = 30)
    private String verifiedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    @Builder
    public EmailVerification(String email, String token, String code) {
        this.email = email;
        this.token = token;
        this.code = code;
        this.expiresAt = Instant.now().plusSeconds(10 * 60); // 10분 후 만료
        this.isVerified = false;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public void verifyCode(String code) {
        if (this.code.equals(code) && !isExpired()) {
            setVerified("CODE");
            return;
        }

        throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }

    public void verifyToken() {
        if (!isExpired()) {
            setVerified("TOKEN");
            return;
        }

        throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }

    public void regenerate(String newCode, String newToken) {
        if (this.isVerified) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.ALREADY_VERIFIED_EMAIL);
        }

        this.code = newCode;
        this.token = newToken;
        this.expiresAt = Instant.now().plusSeconds(10 * 60);
    }

    private void setVerified(String method) {
        this.isVerified = true;
        this.verifiedAt = Instant.now();
        this.verifiedBy = method;
    }
}
