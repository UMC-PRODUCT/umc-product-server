package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WithdrawnMemberIdentity 도메인")
class WithdrawnMemberIdentityTest {

    @Test
    @DisplayName("탈퇴 회원의 기존 ID와 이메일 식별키를 보존한다")
    void 탈퇴_회원의_기존_ID와_이메일_식별키를_보존한다() {
        // given
        String emailKey = "a".repeat(64);

        // when
        WithdrawnMemberIdentity identity = WithdrawnMemberIdentity.create(1L, emailKey);

        // then
        assertThat(identity.getFormerMemberId()).isEqualTo(1L);
        assertThat(identity.getEmailKey()).isEqualTo(emailKey);
    }

    @Test
    @DisplayName("이메일 식별키는 SHA-256 길이여야 한다")
    void 이메일_식별키는_SHA_256_길이여야_한다() {
        assertThatThrownBy(() -> WithdrawnMemberIdentity.create(
            1L,
            "too-short"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이메일 식별키는 소문자 16진수여야 한다")
    void 이메일_식별키는_소문자_16진수여야_한다() {
        assertThatThrownBy(() -> WithdrawnMemberIdentity.create(
            1L,
            "z".repeat(64)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
