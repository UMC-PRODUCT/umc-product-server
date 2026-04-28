package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.authentication.application.port.in.command.dto.ChangePasswordCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LoginByIdPwCommand;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialCommand;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.member.application.port.in.command.ManageMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCredentialCommand;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * CredentialAuthenticationService 단위 테스트.
 * <p>
 * 외부 협력자(PasswordEncoder, JwtTokenProvider, Member 측 UseCase)는 모두 mock 으로 두고 Service 내부의 분기/사용자 열거 방지 / rehash 정책을
 * 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class CredentialAuthenticationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String LOGIN_ID = "alice01";
    private static final String RAW_PASSWORD = "Strong-Pw-2026";
    private static final String ENCODED_PASSWORD = "{argon2}$argon2id$v=19$m=16384,t=2,p=1$abc$def";
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    GetMemberCredentialUseCase getMemberCredentialUseCase;
    @Mock
    ManageMemberCredentialUseCase manageMemberCredentialUseCase;
    @InjectMocks
    CredentialAuthenticationService service;

    @Nested
    @DisplayName("자격 증명 등록")
    class RegisterCredential {

        @Test
        @DisplayName("loginId 중복이 없으면 인코딩된 비밀번호로 회원 자격증명을 등록한다")
        void 중복없으면_등록_성공() {
            // given
            given(getMemberCredentialUseCase.existsByLoginId(LOGIN_ID)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            RegisterCredentialCommand command = RegisterCredentialCommand.of(MEMBER_ID, LOGIN_ID, RAW_PASSWORD);

            // when
            service.registerCredential(command);

            // then
            ArgumentCaptor<RegisterMemberCredentialCommand> captor =
                ArgumentCaptor.forClass(RegisterMemberCredentialCommand.class);
            then(manageMemberCredentialUseCase).should().registerCredential(captor.capture());
            RegisterMemberCredentialCommand captured = captor.getValue();
            assertThat(captured.memberId()).isEqualTo(MEMBER_ID);
            assertThat(captured.loginId()).isEqualTo(LOGIN_ID);
            assertThat(captured.encodedPassword()).isEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("loginId 가 이미 사용 중이면 LOGIN_ID_ALREADY_EXISTS 예외 (인코딩/저장 미호출)")
        void 중복이면_예외() {
            // given
            given(getMemberCredentialUseCase.existsByLoginId(LOGIN_ID)).willReturn(true);

            RegisterCredentialCommand command = RegisterCredentialCommand.of(MEMBER_ID, LOGIN_ID, RAW_PASSWORD);

            // when & then
            assertThatThrownBy(() -> service.registerCredential(command))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.LOGIN_ID_ALREADY_EXISTS);

            then(passwordEncoder).should(never()).encode(anyString());
            then(manageMemberCredentialUseCase).should(never()).registerCredential(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 교체한다")
        void 비밀번호_변경_성공() {
            // given
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, ENCODED_PASSWORD);
            given(getMemberCredentialUseCase.findCredentialByMemberId(MEMBER_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(passwordEncoder.encode("New-Pw-2026")).willReturn("{argon2}new-hash");

            ChangePasswordCommand command = ChangePasswordCommand.of(MEMBER_ID, RAW_PASSWORD, "New-Pw-2026");

            // when
            service.changePassword(command);

            // then
            ArgumentCaptor<ChangeMemberPasswordCommand> captor =
                ArgumentCaptor.forClass(ChangeMemberPasswordCommand.class);
            then(manageMemberCredentialUseCase).should().changePassword(captor.capture());
            assertThat(captor.getValue().memberId()).isEqualTo(MEMBER_ID);
            assertThat(captor.getValue().encodedPassword()).isEqualTo("{argon2}new-hash");
        }

        @Test
        @DisplayName("자격증명이 등록되지 않은 회원은 INVALID_LOGIN_CREDENTIAL 로 응답한다")
        void 자격증명_미등록이면_단일_메시지() {
            // given
            given(getMemberCredentialUseCase.findCredentialByMemberId(MEMBER_ID))
                .willReturn(Optional.empty());

            ChangePasswordCommand command = ChangePasswordCommand.of(MEMBER_ID, RAW_PASSWORD, "New-Pw-2026");

            // when & then
            assertThatThrownBy(() -> service.changePassword(command))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);

            then(passwordEncoder).should(never()).encode(anyString());
            then(manageMemberCredentialUseCase).should(never()).changePassword(any());
        }

        @Test
        @DisplayName("현재 비밀번호가 다르면 INVALID_LOGIN_CREDENTIAL 로 응답한다")
        void 현재_비밀번호_불일치면_단일_메시지() {
            // given
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, ENCODED_PASSWORD);
            given(getMemberCredentialUseCase.findCredentialByMemberId(MEMBER_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(false);

            ChangePasswordCommand command = ChangePasswordCommand.of(MEMBER_ID, RAW_PASSWORD, "New-Pw-2026");

            // when & then
            assertThatThrownBy(() -> service.changePassword(command))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);

            then(manageMemberCredentialUseCase).should(never()).changePassword(any());
        }
    }

    @Nested
    @DisplayName("ID/PW 로그인")
    class LoginByIdPw {

        @Test
        @DisplayName("정상 로그인 시 토큰을 발급하고, 정책 갱신이 불필요하면 rehash 하지 않는다")
        void 로그인_성공_rehash_불필요() {
            // given
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, ENCODED_PASSWORD);
            given(getMemberCredentialUseCase.findCredentialByLoginId(LOGIN_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
            given(passwordEncoder.upgradeEncoding(ENCODED_PASSWORD)).willReturn(false);
            given(jwtTokenProvider.createAccessToken(eq(MEMBER_ID), anyList())).willReturn("access-token");
            given(jwtTokenProvider.createRefreshToken(MEMBER_ID)).willReturn("refresh-token");

            // when
            IdPwLoginResult result = service.loginByIdPw(LoginByIdPwCommand.of(LOGIN_ID, RAW_PASSWORD));

            // then
            assertThat(result.memberId()).isEqualTo(MEMBER_ID);
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            then(manageMemberCredentialUseCase).should(never()).changePassword(any());
        }

        @Test
        @DisplayName("upgradeEncoding 이 true 이면 새 해시로 점진적 rehash 를 수행한다")
        void 로그인_성공시_정책_갱신_필요하면_rehash() {
            // given
            String legacyHash = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, legacyHash);
            given(getMemberCredentialUseCase.findCredentialByLoginId(LOGIN_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, legacyHash)).willReturn(true);
            given(passwordEncoder.upgradeEncoding(legacyHash)).willReturn(true);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(jwtTokenProvider.createAccessToken(eq(MEMBER_ID), anyList())).willReturn("access-token");
            given(jwtTokenProvider.createRefreshToken(MEMBER_ID)).willReturn("refresh-token");

            // when
            IdPwLoginResult result = service.loginByIdPw(LoginByIdPwCommand.of(LOGIN_ID, RAW_PASSWORD));

            // then
            assertThat(result.accessToken()).isEqualTo("access-token");
            ArgumentCaptor<ChangeMemberPasswordCommand> captor =
                ArgumentCaptor.forClass(ChangeMemberPasswordCommand.class);
            then(manageMemberCredentialUseCase).should().changePassword(captor.capture());
            assertThat(captor.getValue().memberId()).isEqualTo(MEMBER_ID);
            assertThat(captor.getValue().encodedPassword()).isEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("rehash 저장 실패는 로그인 자체에 영향을 주지 않는다")
        void rehash_실패해도_로그인_성공() {
            // given
            String legacyHash = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, legacyHash);
            given(getMemberCredentialUseCase.findCredentialByLoginId(LOGIN_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, legacyHash)).willReturn(true);
            given(passwordEncoder.upgradeEncoding(legacyHash)).willReturn(true);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(jwtTokenProvider.createAccessToken(eq(MEMBER_ID), anyList())).willReturn("access-token");
            given(jwtTokenProvider.createRefreshToken(MEMBER_ID)).willReturn("refresh-token");
            // rehash 시도 시 예외 발생
            org.mockito.BDDMockito.willThrow(new RuntimeException("DB error"))
                .given(manageMemberCredentialUseCase).changePassword(any());

            // when
            IdPwLoginResult result = service.loginByIdPw(LoginByIdPwCommand.of(LOGIN_ID, RAW_PASSWORD));

            // then: 토큰은 정상 발급되어야 한다
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("자격증명을 찾을 수 없으면 INVALID_LOGIN_CREDENTIAL 단일 메시지를 반환한다")
        void 자격증명_없으면_단일_메시지() {
            // given
            given(getMemberCredentialUseCase.findCredentialByLoginId(LOGIN_ID))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.loginByIdPw(LoginByIdPwCommand.of(LOGIN_ID, RAW_PASSWORD)))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);

            // 매칭/토큰 발급은 호출되지 않아야 한다
            then(passwordEncoder).should(never()).matches(anyString(), anyString());
            then(jwtTokenProvider).should(never()).createAccessToken(anyLong(), anyList());
        }

        @Test
        @DisplayName("비밀번호가 다르면 INVALID_LOGIN_CREDENTIAL 단일 메시지를 반환한다")
        void 비밀번호_불일치면_단일_메시지() {
            // given
            MemberCredentialInfo credential = new MemberCredentialInfo(MEMBER_ID, LOGIN_ID, ENCODED_PASSWORD);
            given(getMemberCredentialUseCase.findCredentialByLoginId(LOGIN_ID))
                .willReturn(Optional.of(credential));
            given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> service.loginByIdPw(LoginByIdPwCommand.of(LOGIN_ID, RAW_PASSWORD)))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);

            then(jwtTokenProvider).should(never()).createAccessToken(anyLong(), anyList());
            then(manageMemberCredentialUseCase).should(never()).changePassword(any());
        }
    }
}
