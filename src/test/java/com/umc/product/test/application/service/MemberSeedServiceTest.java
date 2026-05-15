package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;
import java.util.List;
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
    RegisterIdPwMemberUseCase registerIdPwMemberUseCase;
    @Mock
    RegisterOAuthMemberUseCase registerOAuthMemberUseCase;
    @Mock
    DummyMemberFactory dummyMemberFactory;

    MemberSeedService sut;
    SeedProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SeedProperties(
            true,
            0L,
            5,
            6,
            "alpha.umc.test",
            "Alpha!Pass2026"
        );
        sut = new MemberSeedService(
            properties,
            dummyMemberFactory,
            getMemberUseCase,
            registerIdPwMemberUseCase,
            registerOAuthMemberUseCase
        );
    }

    @Test
    @DisplayName("force=false 이고 회원 수가 임계값 초과면 시딩을 스킵한다")
    void 멱등성_초과시_스킵() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(10L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(5, 5, false));

        // Then
        assertThat(result.skipped()).isTrue();
        assertThat(result.reason()).contains("threshold");
        verify(registerIdPwMemberUseCase, never()).register(any());
        verify(registerOAuthMemberUseCase, never()).batchRegister(any());
    }

    @Test
    @DisplayName("force=true 면 임계값 체크를 무시하고 시딩한다")
    void force_true_면_임계값_무시() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(100L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(mock(IdPwRegisterMemberCommand.class));
        given(dummyMemberFactory.nextOAuthCommands(anyInt())).willReturn(List.of(mock(OAuthRegisterMemberCommand.class)));
        given(registerOAuthMemberUseCase.batchRegister(any())).willReturn(List.of(1L));

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(2, 1, true));

        // Then
        assertThat(result.skipped()).isFalse();
        assertThat(result.registeredIdPw()).isEqualTo(2);
        assertThat(result.registeredOAuth()).isEqualTo(1);
    }

    @Test
    @DisplayName("정상 시딩 시 ID/PW 는 단건 호출 N회, OAuth 는 batch 1회 호출")
    void 정상_시딩_호출_횟수() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(mock(IdPwRegisterMemberCommand.class));
        given(dummyMemberFactory.nextOAuthCommands(6)).willReturn(List.of(
            mock(OAuthRegisterMemberCommand.class),
            mock(OAuthRegisterMemberCommand.class),
            mock(OAuthRegisterMemberCommand.class),
            mock(OAuthRegisterMemberCommand.class),
            mock(OAuthRegisterMemberCommand.class),
            mock(OAuthRegisterMemberCommand.class)
        ));
        given(registerOAuthMemberUseCase.batchRegister(any())).willReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        // When
        sut.seed(new SeedMembersCommand(5, 6, false));

        // Then
        verify(registerIdPwMemberUseCase, times(5)).register(any());
        verify(registerOAuthMemberUseCase, times(1)).batchRegister(any());
    }

    @Test
    @DisplayName("ID/PW 단건 실패는 다음 시딩 호출을 막지 않는다")
    void idpw_실패_격리() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(mock(IdPwRegisterMemberCommand.class));
        given(registerIdPwMemberUseCase.register(any()))
            .willReturn(1L)
            .willThrow(new RuntimeException("boom"))
            .willReturn(3L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(3, 0, false));

        // Then
        verify(registerIdPwMemberUseCase, times(3)).register(any());
        assertThat(result.registeredIdPw()).isEqualTo(2);
    }

    @Test
    @DisplayName("idPwCount=0, oauthCount=0 이면 어떤 UseCase 도 호출하지 않는다")
    void count_0_미호출() {
        // Given
        given(getMemberUseCase.countAll()).willReturn(0L);

        // When
        SeedMembersResult result = sut.seed(new SeedMembersCommand(0, 0, false));

        // Then
        assertThat(result.skipped()).isFalse();
        assertThat(result.registeredIdPw()).isZero();
        assertThat(result.registeredOAuth()).isZero();
        verify(registerIdPwMemberUseCase, never()).register(any());
        verify(registerOAuthMemberUseCase, never()).batchRegister(any());
    }

    private static <T> T mock(Class<T> type) {
        return org.mockito.Mockito.mock(type);
    }
}
