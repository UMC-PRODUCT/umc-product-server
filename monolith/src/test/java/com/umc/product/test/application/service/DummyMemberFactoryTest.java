package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.term.application.port.in.query.GetTermUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("더미 회원 닉네임 생성")
class DummyMemberFactoryTest {

    @Mock
    SeedProperties properties;

    @Mock
    GetTermUseCase getTermUseCase;

    @InjectMocks
    DummyMemberFactory factory;

    @Test
    @DisplayName("연속 시퀀스의 닉네임은 모두 한글 4자이며 중복되지 않는다")
    void 시퀀스별_유효한_고유_닉네임을_만든다() {
        // given
        Set<String> nicknames = new HashSet<>();

        // when & then
        for (long sequence = 0; sequence < 20_000; sequence++) {
            String nickname = factory.nextEmailCommandWithSchool(sequence, 1L, List.of()).nickname();
            assertThat(nickname).matches("^[가-힣]{4}$");
            assertThat(nicknames.add(nickname)).isTrue();
        }
    }

    @Test
    @DisplayName("같은 시퀀스는 같은 닉네임으로 재생성한다")
    void 동일_시퀀스는_동일_닉네임을_만든다() {
        // given & when
        var first = factory.nextEmailCommandWithSchool(11_172L, 1L, List.of());
        var second = factory.nextEmailCommandWithSchool(11_172L, 1L, List.of());

        // then
        assertThat(first.nickname()).isEqualTo(second.nickname());
    }

    @Test
    @DisplayName("표현 범위 밖 시퀀스를 잘라 중복 닉네임으로 만들지 않는다")
    void 시퀀스_범위를_벗어나면_거절한다() {
        // given & when & then
        assertThatThrownBy(() -> factory.nextEmailCommandWithSchool(-1L, 1L, List.of()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.nextEmailCommandWithSchool(Long.MAX_VALUE, 1L, List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
