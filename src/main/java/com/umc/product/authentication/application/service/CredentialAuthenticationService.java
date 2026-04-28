package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
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
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ID/PW 자격증명 등록/변경/로그인을 담당하는 Service.
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
    public void registerCredential(RegisterCredentialCommand command) {
        // 형식 검증은 커맨드 record 에서 이미 끝났다. 여기서는 중복만 본다.
        if (getMemberCredentialUseCase.existsByLoginId(command.loginId())) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(command.rawPassword());

        manageMemberCredentialUseCase.registerCredential(
            RegisterMemberCredentialCommand.of(command.memberId(), command.loginId(), encodedPassword)
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
    @Transactional(readOnly = true)
    public IdPwLoginResult loginByIdPw(LoginByIdPwCommand command) {
        // 1) 자격증명 조회: 부재 / 실패 모두 동일 메시지로 처리하여 사용자 열거 공격을 방지한다.
        Optional<MemberCredentialInfo> credentialOpt =
            getMemberCredentialUseCase.findCredentialByLoginId(command.loginId());

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
