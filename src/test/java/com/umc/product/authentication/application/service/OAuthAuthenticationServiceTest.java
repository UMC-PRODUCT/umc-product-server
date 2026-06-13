package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.RevokeOAuthTokenPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.command.LockMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.MemberCredentialStatusInfo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthAuthenticationService")
class OAuthAuthenticationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long MEMBER_OAUTH_ID = 10L;

    @Mock
    VerifyOAuthTokenPort verifyOAuthTokenPort;

    @Mock
    LoadMemberOAuthPort loadMemberOAuthPort;

    @Mock
    SaveMemberOAuthPort saveMemberOAuthPort;

    @Mock
    RevokeOAuthTokenPort revokeOAuthTokenPort;

    @Mock
    LockMemberCredentialUseCase lockMemberCredentialUseCase;

    @InjectMocks
    OAuthAuthenticationService sut;

    @Test
    @DisplayName("local credential이 있으면 마지막 OAuth도 해제할 수 있다")
    void local_credential이_있으면_마지막_OAuth도_해제할_수_있다() {
        MemberOAuth memberOAuth = memberOAuth(MEMBER_OAUTH_ID, OAuthProvider.GOOGLE);
        given(loadMemberOAuthPort.findByMemberOAuthId(MEMBER_OAUTH_ID)).willReturn(Optional.of(memberOAuth));
        given(lockMemberCredentialUseCase.getCredentialStatusForUpdate(MEMBER_ID))
            .willReturn(new MemberCredentialStatusInfo(MEMBER_ID, true));

        sut.unlinkOAuth(command(false));

        then(lockMemberCredentialUseCase).should().getCredentialStatusForUpdate(MEMBER_ID);
        then(loadMemberOAuthPort).should(never()).findAllByMemberId(anyLong());
        then(saveMemberOAuthPort).should().delete(memberOAuth);
        verifyNoInteractions(revokeOAuthTokenPort);
    }

    @Test
    @DisplayName("local credential이 없고 마지막 OAuth이면 해제할 수 없다")
    void local_credential이_없고_마지막_OAuth이면_해제할_수_없다() {
        MemberOAuth memberOAuth = memberOAuth(MEMBER_OAUTH_ID, OAuthProvider.GOOGLE);
        given(loadMemberOAuthPort.findByMemberOAuthId(MEMBER_OAUTH_ID)).willReturn(Optional.of(memberOAuth));
        given(lockMemberCredentialUseCase.getCredentialStatusForUpdate(MEMBER_ID))
            .willReturn(new MemberCredentialStatusInfo(MEMBER_ID, false));
        given(loadMemberOAuthPort.findAllByMemberId(MEMBER_ID)).willReturn(List.of(memberOAuth));

        assertThatThrownBy(() -> sut.unlinkOAuth(command(false)))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.OAUTH_CANNOT_UNLINK_LAST_PROVIDER);

        then(saveMemberOAuthPort).should(never()).delete(any());
        verifyNoInteractions(revokeOAuthTokenPort);
    }

    @Test
    @DisplayName("local credential이 없어도 다른 OAuth가 남으면 해제할 수 있다")
    void local_credential이_없어도_다른_OAuth가_남으면_해제할_수_있다() {
        MemberOAuth targetOAuth = memberOAuth(MEMBER_OAUTH_ID, OAuthProvider.GOOGLE);
        MemberOAuth remainingOAuth = memberOAuth(11L, OAuthProvider.KAKAO);
        given(loadMemberOAuthPort.findByMemberOAuthId(MEMBER_OAUTH_ID)).willReturn(Optional.of(targetOAuth));
        given(lockMemberCredentialUseCase.getCredentialStatusForUpdate(MEMBER_ID))
            .willReturn(new MemberCredentialStatusInfo(MEMBER_ID, false));
        given(loadMemberOAuthPort.findAllByMemberId(MEMBER_ID)).willReturn(List.of(targetOAuth, remainingOAuth));

        sut.unlinkOAuth(command(false));

        then(saveMemberOAuthPort).should().delete(targetOAuth);
        verifyNoInteractions(revokeOAuthTokenPort);
    }

    @Test
    @DisplayName("탈퇴 흐름은 credential과 마지막 OAuth 검사를 건너뛰고 해제한다")
    void 탈퇴_흐름은_credential과_마지막_OAuth_검사를_건너뛰고_해제한다() {
        MemberOAuth memberOAuth = memberOAuth(MEMBER_OAUTH_ID, OAuthProvider.GOOGLE);
        given(loadMemberOAuthPort.findByMemberOAuthId(MEMBER_OAUTH_ID)).willReturn(Optional.of(memberOAuth));

        sut.unlinkOAuth(command(true));

        then(lockMemberCredentialUseCase).should(never()).getCredentialStatusForUpdate(anyLong());
        then(loadMemberOAuthPort).should(never()).findAllByMemberId(anyLong());
        then(saveMemberOAuthPort).should().delete(memberOAuth);
        verifyNoInteractions(revokeOAuthTokenPort);
    }

    private UnlinkOAuthCommand command(boolean withdrawal) {
        return UnlinkOAuthCommand.builder()
            .memberId(MEMBER_ID)
            .memberOAuthId(MEMBER_OAUTH_ID)
            .isWithdrawal(withdrawal)
            .build();
    }

    private MemberOAuth memberOAuth(Long id, OAuthProvider provider) {
        MemberOAuth memberOAuth = MemberOAuth.builder()
            .memberId(MEMBER_ID)
            .provider(provider)
            .providerId(provider.name().toLowerCase() + "-provider-id-" + id)
            .build();
        ReflectionTestUtils.setField(memberOAuth, "id", id);
        return memberOAuth;
    }
}
