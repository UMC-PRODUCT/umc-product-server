package com.umc.product.authorization.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.member.domain.MemberSystemRoleType;

@DisplayName("SystemRoleType")
class SystemRoleTypeTest {

    @Test
    @DisplayName("저장용 MemberSystemRoleType과 같은 값을 가진다")
    void 저장용_enum과_같은_값을_가진다() {
        assertThat(Arrays.stream(SystemRoleType.values()).map(Enum::name))
            .containsExactlyInAnyOrderElementsOf(
                Arrays.stream(MemberSystemRoleType.values()).map(Enum::name).toList());
    }
}
