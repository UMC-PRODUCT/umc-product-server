package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.RevokeOAuthTokenPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuthAuthenticationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long MEMBER_OAUTH_ID = 10L;
    private static final String STORED_GOOGLE_PROVIDER_ID = "google-user-1";
    private static final String OTHER_GOOGLE_PROVIDER_ID = "google-user-2";
    private static final String GOOGLE_ACCESS_TOKEN = "google-access-token";

    @Mock
    VerifyOAuthTokenPort verifyOAuthTokenPort;

    @Mock
    LoadMemberOAuthPort loadMemberOAuthPort;

    @Mock
    SaveMemberOAuthPort saveMemberOAuthPort;

    @Mock
    RevokeOAuthTokenPort revokeOAuthTokenPort;

    @InjectMocks
    OAuthAuthenticationService service;

    @Nested
    @DisplayName("unlinkOAuth")
    class UnlinkOAuth {

        @Test
        @DisplayName("Google access token의 providerId가 연동 계정과 다르면 연동 해제를 거부한다")
        void google_access_token_providerId_불일치_거부() {
            // given
            MemberOAuth memberOAuth = memberOAuth(
                MEMBER_OAUTH_ID,
                MEMBER_ID,
                OAuthProvider.GOOGLE,
                STORED_GOOGLE_PROVIDER_ID
            );
            given(loadMemberOAuthPort.findByMemberOAuthId(MEMBER_OAUTH_ID))
                .willReturn(Optional.of(memberOAuth));
            given(verifyOAuthTokenPort.verify(OAuthProvider.GOOGLE, GOOGLE_ACCESS_TOKEN))
                .willReturn(googleAttributes(OTHER_GOOGLE_PROVIDER_ID));

            UnlinkOAuthCommand command = UnlinkOAuthCommand.builder()
                .memberId(MEMBER_ID)
                .memberOAuthId(MEMBER_OAUTH_ID)
                .isWithdrawal(true)
                .googleAccessToken(GOOGLE_ACCESS_TOKEN)
                .build();

            // when & then
            assertThatThrownBy(() -> service.unlinkOAuth(command))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.OAUTH_INVALID_ACCESS_TOKEN);

            then(revokeOAuthTokenPort).should(never()).revokeGoogleToken(anyString());
            then(saveMemberOAuthPort).should(never()).delete(any());
        }
    }

    private MemberOAuth memberOAuth(Long id, Long memberId, OAuthProvider provider, String providerId) {
        MemberOAuth memberOAuth = MemberOAuth.builder()
            .memberId(memberId)
            .provider(provider)
            .providerId(providerId)
            .build();
        ReflectionTestUtils.setField(memberOAuth, "id", id);
        return memberOAuth;
    }

    private OAuthAttributes googleAttributes(String providerId) {
        return OAuthAttributes.of("google", Map.of(
            "sub", providerId,
            "email", "google@example.com"
        ));
    }
}
