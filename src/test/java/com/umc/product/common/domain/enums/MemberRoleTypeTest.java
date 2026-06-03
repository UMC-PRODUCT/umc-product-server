package com.umc.product.common.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberRoleTypeTest {

    @Test
    @DisplayName("ADMIN은 NORMAL 이상의 권한을 가진다")
    void ADMIN은_NORMAL_이상이다() {
        assertThat(MemberRoleType.ADMIN.isAtLeast(MemberRoleType.NORMAL)).isTrue();
    }

    @Test
    @DisplayName("NORMAL은 ADMIN 이상의 권한을 가지지 않는다")
    void NORMAL은_ADMIN_이상이_아니다() {
        assertThat(MemberRoleType.NORMAL.isAtLeast(MemberRoleType.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("ADMIN만 전역 관리자 권한으로 판단한다")
    void ADMIN만_관리자이다() {
        assertThat(MemberRoleType.ADMIN.isAdmin()).isTrue();
        assertThat(MemberRoleType.NORMAL.isAdmin()).isFalse();
    }
}
