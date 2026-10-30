package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MemberBlock 도메인")
class MemberBlockTest {

    @Test
    @DisplayName("create는 blocker/blocked member id를 담은 차단 관계를 생성한다")
    void create는_차단_관계를_생성한다() {
        MemberBlock block = MemberBlock.create(1L, 2L);

        assertThat(block.getBlockerMemberId()).isEqualTo(1L);
        assertThat(block.getBlockedMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("자기 자신을 차단하면 거절한다")
    void create는_자기_자신을_차단하면_거절한다() {
        assertThatThrownBy(() -> MemberBlock.create(1L, 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("blockerMemberId가 없으면 거절한다")
    void create는_blockerMemberId가_없으면_거절한다() {
        assertThatThrownBy(() -> MemberBlock.create(null, 2L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("blockedMemberId가 없으면 거절한다")
    void create는_blockedMemberId가_없으면_거절한다() {
        assertThatThrownBy(() -> MemberBlock.create(1L, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
