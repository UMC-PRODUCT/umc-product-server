package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.ChangePasswordCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LoginByEmailCommand;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialByEmailCommand;
import com.umc.product.authentication.application.port.in.command.dto.ResetPasswordByEmailCommand;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.member.application.port.in.command.ManageMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCredentialByEmailCommand;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일/PW 자격증명 등록/변경/로그인을 담당하는 Service. ADR-017 흐름.
 * <p>
 * 평문 비밀번호 검증과 {@link PasswordEncoder} 인코딩은 본 Service 의 책임이며,
 * Member 도메인에는 이미 인코딩된 "{id}encoded" 해시만 전달한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CredentialAuthenticationService implements CredentialAuthenticationUseCase {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GetMemberCredentialUseCase getMemberCredentialUseCase;
    private final ManageMemberCredentialUseCase manageMemberCredentialUseCase;
    private final CredentialRehashService rehashService;

    @Override
    public void registerCredentialByEmail(RegisterCredentialByEmailCommand command) {
        // email 중복은 Member.email 의 UNIQUE 제약으로 이미 보장된다.
        // 본 메서드는 Member 가 이미 생성된 직후 또는 기존 회원에 비밀번호를 부착하는 흐름에서 호출되므로
        // 별도 중복 체크를 하지 않는다.
        String encodedPassword = passwordEncoder.encode(command.rawPassword());

        manageMemberCredentialUseCase.registerCredentialByEmail(
            RegisterMemberCredentialByEmailCommand.of(command.memberId(), encodedPassword)
        );
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        // 자격증명 미등록 / 현재 비밀번호 불일치는 동일한 단일 메시지로 응답한다.
        MemberCredentialInfo credential = getMemberCredentialUseCase
            .findCredentialByMemberId(command.memberId())
            .orElseThrow(() -> new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL));

        if (!passwordEncoder.matches(command.currentRawPassword(), credential.passwordHash())) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);
        }

        String newEncodedPassword = passwordEncoder.encode(command.newRawPassword());

        manageMemberCredentialUseCase.changePassword(
            ChangeMemberPasswordCommand.of(command.memberId(), newEncodedPassword)
        );
    }

    @Override
    public void resetPasswordByEmail(ResetPasswordByEmailCommand command) {
        // 이메일은 emailVerificationToken 으로 사전 검증되어 들어오므로 해당 이메일 소유자가 호출했다고 간주한다.
        // 다만 시스템에 가입되지 않은 이메일이거나 자격증명이 등록되지 않은 회원(OAuth 전용 등) 인 경우는
        // 사용자 열거 방지를 위해 단일 메시지로 응답한다.
        MemberCredentialInfo credential = getMemberCredentialUseCase
            .findCredentialByEmail(command.email())
            .orElseThrow(() -> new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL));

        String newEncodedPassword = passwordEncoder.encode(command.newRawPassword());

        manageMemberCredentialUseCase.changePassword(
            ChangeMemberPasswordCommand.of(credential.memberId(), newEncodedPassword)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public IdPwLoginResult loginByEmail(LoginByEmailCommand command) {
        // 1) 자격증명 조회: 부재 / 실패 모두 동일 메시지로 처리하여 사용자 열거 공격을 방지한다.
        Optional<MemberCredentialInfo> credentialOpt =
            getMemberCredentialUseCase.findCredentialByEmail(command.email());

        if (credentialOpt.isEmpty()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);
        }

        MemberCredentialInfo credential = credentialOpt.get();

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(command.rawPassword(), credential.passwordHash())) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_CREDENTIAL);
        }

        // 3) 점진적 rehash: 해시 정책이 갱신되었으면 최신 정책으로 재저장한다.
        // 별도 트랜잭션(REQUIRES_NEW)에서 수행하여 실패가 로그인에 영향을 주지 않도록 한다.
        rehashService.rehashIfNeeded(credential, command.rawPassword());

        // 4) 토큰 발급
        Long memberId = credential.memberId();
        String accessToken = jwtTokenProvider.createAccessToken(memberId, Collections.emptyList());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        return IdPwLoginResult.builder()
            .memberId(memberId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
