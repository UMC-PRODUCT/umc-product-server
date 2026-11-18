package com.umc.product.member.domain;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지원자 이메일로 탈퇴 회원의 과거 탈부 기록을 찾기 위한 식별키 매핑입니다.
 * <p>
 * Member 물리 삭제 전에 검증된 이메일을 {@link MemberRejoinKey}로 SHA-256 변환하여
 * {@code emailKey}에 저장하고, 삭제될 회원 ID를 {@code formerMemberId}에 저장합니다.
 * {@code formerMemberId}에는 Member FK를 설정하지 않으므로 회원 삭제 후에도 값이 유지됩니다.
 * <p>
 * 지원자 이메일을 동일한 방식으로 변환해 {@code formerMemberId}를 찾고,
 * Challenger의 memberId를 통해 과거 탈부 기록을 조회합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "withdrawn_member_identity",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_withdrawn_member_identity_email_member",
        columnNames = {"email_key", "former_member_id"}
    )
)
public class WithdrawnMemberIdentity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "former_member_id", nullable = false)
    private Long formerMemberId;

    @Column(name = "email_key", nullable = false, length = 64)
    private String emailKey;

    @Builder(access = AccessLevel.PRIVATE)
    private WithdrawnMemberIdentity(Long formerMemberId, String emailKey) {
        this.formerMemberId = formerMemberId;
        this.emailKey = emailKey;
    }

    public static WithdrawnMemberIdentity create(Long formerMemberId, String emailKey) {
        if (formerMemberId == null || emailKey == null || emailKey.isBlank()) {
            throw new IllegalArgumentException("formerMemberId and emailKey must not be null or blank");
        }
        if (!emailKey.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("emailKey must be a lowercase SHA-256 hex string");
        }
        return WithdrawnMemberIdentity.builder()
            .formerMemberId(formerMemberId)
            .emailKey(emailKey)
            .build();
    }
}
