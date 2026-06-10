package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.MemberRoleType;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberRoleMutationTest {

    @Test
    @DisplayName("새 회원은 기본 권한이 NORMAL이다")
    void 새_회원은_NORMAL_권한이다() {
        Member member = Member.create("홍길동", "길동", "test@example.com", 1L, null);

        assertThat(member.getRoleType()).isEqualTo(MemberRoleType.NORMAL);
    }

    @Test
    @DisplayName("회원 권한은 도메인 메서드로 변경한다")
    void 회원_권한_변경_성공() {
        Member member = Member.create("홍길동", "길동", "test@example.com", 1L, null);

        member.changeRole(MemberRoleType.ADMIN);

        assertThat(member.getRoleType()).isEqualTo(MemberRoleType.ADMIN);
    }

    @Test
    @DisplayName("null 권한으로 변경할 수 없다")
    void null_권한_변경_거부() {
        Member member = Member.create("홍길동", "길동", "test@example.com", 1L, null);

        assertThatThrownBy(() -> member.changeRole(null))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.INVALID_MEMBER_ROLE);
    }
}
