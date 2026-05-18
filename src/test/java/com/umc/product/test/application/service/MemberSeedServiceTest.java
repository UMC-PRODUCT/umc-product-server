package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberSeedServiceTest {

    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    RegisterEmailMemberUseCase registerEmailMemberUseCase;
    @Mock
    DummyMemberFactory dummyMemberFactory;

    MemberSeedService sut;
    SeedProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SeedProperties(
            true,
            0L,
            "alpha.umc.test",
            "Alpha!Pass2026"
        );
        sut = new MemberSeedService(
            properties,
            dummyMemberFactory,
            getMemberUseCase,
            registerEmailMemberUseCase
        );
    }

    @Test
    @DisplayName("force=false 이고 회원 수가 임계값 초과면 시딩을 스킵한다")
    void 멱등성_초과시_스킵() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(10L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(5, false));

        // Then
        assertThat(result.skipped()).isTrue();
        assertThat(result.reason()).contains("threshold");
        verify(registerEmailMemberUseCase, never()).register(any());
    }

    @Test
    @DisplayName("force=true 면 임계값 체크를 무시하고 시딩한다")
    void force_true_면_임계값_무시() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(100L);
        given(dummyMemberFactory.nextEmailCommand(anyLong())).willReturn(mock(EmailRegisterMemberCommand.class));

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(2, true));

        // Then
        assertThat(result.skipped()).isFalse();
        assertThat(result.registered()).isEqualTo(2);
    }

    @Test
    @DisplayName("정상 시딩 시 email register 가 count 번 호출된다")
    void 정상_시딩_호출_횟수() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);
        given(dummyMemberFactory.nextEmailCommand(anyLong())).willReturn(mock(EmailRegisterMemberCommand.class));

        // When
        sut.seed(new SeedMembersCommand(5, false));

        // Then
        verify(registerEmailMemberUseCase, times(5)).register(any());
    }

    @Test
    @DisplayName("이메일 단건 실패는 다음 시딩 호출을 막지 않는다")
    void email_실패_격리() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);
        given(dummyMemberFactory.nextEmailCommand(anyLong())).willReturn(mock(EmailRegisterMemberCommand.class));
        given(registerEmailMemberUseCase.register(any()))
            .willReturn(1L)
            .willThrow(new RuntimeException("boom"))
            .willReturn(3L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(3, false));

        // Then
        verify(registerEmailMemberUseCase, times(3)).register(any());
        assertThat(result.registered()).isEqualTo(2);
    }

    @Test
    @DisplayName("count=0 이면 register 가 호출되지 않는다")
    void count_0_미호출() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(0, false));

        // Then
        assertThat(result.skipped()).isFalse();
        assertThat(result.registered()).isZero();
        verify(registerEmailMemberUseCase, never()).register(any());
    }

    @Test
    @DisplayName("email 시퀀스는 현재 회원 수 + 1 부터 시작한다")
    void 시퀀스_오프셋_확인() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(42L);
        given(dummyMemberFactory.nextEmailCommand(anyLong())).willReturn(mock(EmailRegisterMemberCommand.class));

        // When
        sut.seed(new SeedMembersCommand(2, true));

        // Then
        verify(dummyMemberFactory).nextEmailCommand(43L);
        verify(dummyMemberFactory).nextEmailCommand(44L);
    }
}
