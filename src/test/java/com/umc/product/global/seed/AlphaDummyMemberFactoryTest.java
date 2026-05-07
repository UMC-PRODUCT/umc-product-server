package com.umc.product.global.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.given;

import com.umc.product.authentication.domain.CredentialPolicy;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlphaDummyMemberFactoryTest {

    @Mock
    GetTermUseCase getTermUseCase;

    AlphaDummyMemberFactory sut;

    @BeforeEach
    void setUp() {
        AlphaSeedProperties properties = new AlphaSeedProperties(
            true,
            0L,
            20,
            30,
            "alpha.umc.test",
            "Alpha!Pass2026"
        );
        given(getTermUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));
        sut = new AlphaDummyMemberFactory(properties, getTermUseCase);
    }

    @Test
    @DisplayName("nextIdPwCommand 의 loginId 는 alpha_user_{4자리시퀀스} 형식이며 email 은 그 loginId@emailDomain")
    void idPw_loginId_email_형식() {
        IdPwRegisterMemberCommand cmd = sut.nextIdPwCommand(7);

        assertThat(cmd.loginId()).isEqualTo("alpha_user_0007");
        assertThat(cmd.email()).isEqualTo("alpha_user_0007@alpha.umc.test");
        assertThat(cmd.rawPassword()).isEqualTo("Alpha!Pass2026");
    }

    @Test
    @DisplayName("nextIdPwCommand 가 만든 loginId/password 는 CredentialPolicy 를 그대로 통과한다")
    void idPw_credential_policy_통과() {
        IdPwRegisterMemberCommand cmd = sut.nextIdPwCommand(1);

        assertThatNoException().isThrownBy(() -> CredentialPolicy.validateLoginId(cmd.loginId()));
        assertThatNoException().isThrownBy(() -> CredentialPolicy.validatePassword(cmd.rawPassword()));
    }

    @Test
    @DisplayName("termConsents 는 GetTermUseCase 가 반환한 모든 필수 약관에 대해 isAgreed=true 로 채워진다")
    void termConsents_모두_isAgreed_true() {
        IdPwRegisterMemberCommand cmd = sut.nextIdPwCommand(1);

        assertThat(cmd.termConsents()).hasSize(2);
        assertThat(cmd.termConsents()).allMatch(TermConsents::isAgreed);
        assertThat(cmd.termConsents())
            .extracting(TermConsents::termId)
            .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("schoolId 는 시딩된 master 데이터 범위 1~38 안에 있다")
    void schoolId_범위_1_부터_38() {
        for (int i = 1; i <= 50; i++) {
            IdPwRegisterMemberCommand cmd = sut.nextIdPwCommand(i);
            assertThat(cmd.schoolId()).isBetween(1L, 38L);
        }
    }

    @Test
    @DisplayName("nextOAuthCommands 는 GOOGLE → APPLE → KAKAO 라운드로빈으로 provider 를 분배한다")
    void oauth_provider_라운드로빈_분포() {
        List<OAuthRegisterMemberCommand> cmds = sut.nextOAuthCommands(6);

        assertThat(cmds).extracting(OAuthRegisterMemberCommand::provider)
            .containsExactly(
                OAuthProvider.GOOGLE,
                OAuthProvider.APPLE,
                OAuthProvider.KAKAO,
                OAuthProvider.GOOGLE,
                OAuthProvider.APPLE,
                OAuthProvider.KAKAO
            );
    }

    @Test
    @DisplayName("APPLE provider 만 appleRefreshToken 과 appleClientId(com.umc.product) 가 채워지고 나머지는 null")
    void apple_만_apple_필드_채워짐() {
        List<OAuthRegisterMemberCommand> cmds = sut.nextOAuthCommands(3);

        OAuthRegisterMemberCommand google = cmds.get(0);
        OAuthRegisterMemberCommand apple = cmds.get(1);
        OAuthRegisterMemberCommand kakao = cmds.get(2);

        assertThat(google.provider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(google.appleRefreshToken()).isNull();
        assertThat(google.appleClientId()).isNull();

        assertThat(apple.provider()).isEqualTo(OAuthProvider.APPLE);
        assertThat(apple.appleRefreshToken()).isNotNull();
        assertThat(apple.appleClientId()).isEqualTo("com.umc.product");

        assertThat(kakao.provider()).isEqualTo(OAuthProvider.KAKAO);
        assertThat(kakao.appleRefreshToken()).isNull();
        assertThat(kakao.appleClientId()).isNull();
    }

    @Test
    @DisplayName("(provider, providerId) 조합은 batch 안에서 모두 unique 하다 — uk_member_oauth_provider_provider_id 보호")
    void provider_providerId_조합_unique() {
        List<OAuthRegisterMemberCommand> cmds = sut.nextOAuthCommands(9);

        long unique = cmds.stream()
            .map(c -> c.provider() + ":" + c.providerId())
            .distinct()
            .count();

        assertThat(unique).isEqualTo(9);
    }

    @Test
    @DisplayName("OAuth Command 의 email 도메인은 properties.emailDomain 을 사용한다")
    void oauth_email_도메인_properties_사용() {
        List<OAuthRegisterMemberCommand> cmds = sut.nextOAuthCommands(3);

        assertThat(cmds).allSatisfy(c ->
            assertThat(c.email()).endsWith("@alpha.umc.test")
        );
    }
}
