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

    @Test
    @DisplayName("저장된 모든 역할 값을 해석할 수 있다")
    void 저장된_모든_역할_값을_해석할_수_있다() {
        for (MemberSystemRoleType stored : MemberSystemRoleType.values()) {
            assertThat(SystemRoleType.find(stored.name())).isPresent();
        }
    }

    @Test
    @DisplayName("모르는 값이나 null이면 빈 값을 돌려준다")
    void 모르는_값이나_null이면_빈_값을_돌려준다() {
        assertThat(SystemRoleType.find("NOT_YET_DEPLOYED_ROLE")).isEmpty();
        assertThat(SystemRoleType.find(null)).isEmpty();
    }
}
