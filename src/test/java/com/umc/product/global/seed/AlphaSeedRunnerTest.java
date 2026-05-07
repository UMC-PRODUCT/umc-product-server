package com.umc.product.global.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.out.LoadMemberPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlphaSeedRunnerTest {

    @Mock
    LoadMemberPort loadMemberPort;
    @Mock
    RegisterIdPwMemberUseCase registerIdPwMemberUseCase;
    @Mock
    RegisterOAuthMemberUseCase registerOAuthMemberUseCase;
    @Mock
    AlphaDummyMemberFactory dummyMemberFactory;

    AlphaSeedRunner sut;
    AlphaSeedProperties properties;

    private static final int ID_PW_COUNT = 5;
    private static final int OAUTH_COUNT = 6;

    @BeforeEach
    void setUp() {
        properties = new AlphaSeedProperties(
            true,
            0L,
            ID_PW_COUNT,
            OAUTH_COUNT,
            "alpha.umc.test",
            "Alpha!Pass2026"
        );
        sut = new AlphaSeedRunner(
            properties,
            loadMemberPort,
            registerIdPwMemberUseCase,
            registerOAuthMemberUseCase,
            dummyMemberFactory
        );
    }

    @Test
    @DisplayName("회원 수가 임계값 초과면 시딩 자체를 건너뛴다")
    void 회원_수가_임계값_초과면_시딩_skip() {
        given(loadMemberPort.countAllMembers()).willReturn(1L); // threshold=0 < 1

        sut.run(null);

        verify(registerIdPwMemberUseCase, never()).register(any());
        verify(registerOAuthMemberUseCase, never()).batchRegister(any());
        verify(dummyMemberFactory, never()).nextIdPwCommand(anyInt());
        verify(dummyMemberFactory, never()).nextOAuthCommands(anyInt());
    }

    @Test
    @DisplayName("정상 시딩 시 ID/PW 는 properties 만큼 단건 호출, OAuth 는 batch 1회 호출")
    void 정상_시딩_시_각_use_case_가_정확한_횟수로_호출된다() {
        given(loadMemberPort.countAllMembers()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(dummyIdPwCommand());
        given(dummyMemberFactory.nextOAuthCommands(OAUTH_COUNT))
            .willReturn(dummyOAuthCommands(OAUTH_COUNT));
        given(registerOAuthMemberUseCase.batchRegister(any()))
            .willReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        sut.run(null);

        verify(registerIdPwMemberUseCase, times(ID_PW_COUNT)).register(any());
        verify(registerOAuthMemberUseCase, times(1)).batchRegister(any());
    }

    @Test
    @DisplayName("ID/PW 등록 중 일부가 예외를 던져도 다음 회원 등록과 OAuth 시딩은 계속 진행된다")
    void 일부_idPw_실패해도_나머지_등록과_oauth_시딩_계속() {
        given(loadMemberPort.countAllMembers()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(dummyIdPwCommand());
        given(dummyMemberFactory.nextOAuthCommands(OAUTH_COUNT))
            .willReturn(dummyOAuthCommands(OAUTH_COUNT));
        given(registerIdPwMemberUseCase.register(any()))
            .willThrow(new RuntimeException("first failed"))
            .willReturn(2L)
            .willReturn(3L)
            .willReturn(4L)
            .willReturn(5L);
        given(registerOAuthMemberUseCase.batchRegister(any()))
            .willReturn(List.of(10L, 11L, 12L, 13L, 14L, 15L));

        assertThatNoException().isThrownBy(() -> sut.run(null));

        // 한 번 실패해도 5회 모두 시도된다
        verify(registerIdPwMemberUseCase, times(ID_PW_COUNT)).register(any());
        // OAuth 단계도 정상 진입
        verify(registerOAuthMemberUseCase, times(1)).batchRegister(any());
    }

    @Test
    @DisplayName("OAuth batch 가 실패해도 ID/PW 시딩 결과는 보존되며 부팅이 막히지 않는다")
    void oauth_batch_실패해도_부팅_차단_없이_swallow() {
        given(loadMemberPort.countAllMembers()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(dummyIdPwCommand());
        given(dummyMemberFactory.nextOAuthCommands(OAUTH_COUNT))
            .willReturn(dummyOAuthCommands(OAUTH_COUNT));
        given(registerOAuthMemberUseCase.batchRegister(any()))
            .willThrow(new RuntimeException("batch failed"));

        assertThatNoException().isThrownBy(() -> sut.run(null));

        verify(registerIdPwMemberUseCase, times(ID_PW_COUNT)).register(any());
    }

    @Test
    @DisplayName("oauthMemberCount=0 이면 OAuth UseCase 호출이 발생하지 않는다")
    void oauth_count_0이면_batch_호출_없음() {
        properties = new AlphaSeedProperties(
            true, 0L, ID_PW_COUNT, 0, "alpha.umc.test", "Alpha!Pass2026"
        );
        sut = new AlphaSeedRunner(
            properties, loadMemberPort, registerIdPwMemberUseCase,
            registerOAuthMemberUseCase, dummyMemberFactory
        );
        given(loadMemberPort.countAllMembers()).willReturn(0L);
        given(dummyMemberFactory.nextIdPwCommand(anyInt())).willReturn(dummyIdPwCommand());

        sut.run(null);

        verify(registerOAuthMemberUseCase, never()).batchRegister(any());
        verify(dummyMemberFactory, never()).nextOAuthCommands(anyInt());
    }

    @Test
    @DisplayName("AlphaSeedProperties 자체가 정상 record 로 동작하는지 sanity check")
    void properties_record_sanity_check() {
        AlphaSeedProperties p = new AlphaSeedProperties(
            true, 100L, 20, 30, "alpha.umc.test", "Alpha!Pass2026"
        );
        assertThat(p.enabled()).isTrue();
        assertThat(p.skipIfMemberCountGreaterThan()).isEqualTo(100L);
        assertThat(p.idPwMemberCount()).isEqualTo(20);
        assertThat(p.oauthMemberCount()).isEqualTo(30);
    }

    private IdPwRegisterMemberCommand dummyIdPwCommand() {
        return IdPwRegisterMemberCommand.builder()
            .loginId("alpha_user_0001")
            .rawPassword("Alpha!Pass2026")
            .name("홍길동")
            .nickname("길동1")
            .email("alpha_user_0001@alpha.umc.test")
            .schoolId(1L)
            .termConsents(List.of(
                TermConsents.builder().termId(1L).isAgreed(true).build(),
                TermConsents.builder().termId(2L).isAgreed(true).build()
            ))
            .build();
    }

    private List<OAuthRegisterMemberCommand> dummyOAuthCommands(int count) {
        OAuthProvider[] providers = {
            OAuthProvider.GOOGLE, OAuthProvider.APPLE, OAuthProvider.KAKAO
        };
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> OAuthRegisterMemberCommand.builder()
                .provider(providers[(i - 1) % providers.length])
                .providerId("alpha_test_%04d".formatted(i))
                .name("테스트")
                .nickname("닉" + i)
                .email("alpha_oauth_%04d@alpha.umc.test".formatted(i))
                .schoolId(1L)
                .profileImageId(null)
                .termConsents(List.of())
                .appleRefreshToken(null)
                .appleClientId(null)
                .build())
            .toList();
    }
}
