package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.ChangePasswordCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LoginByEmailCommand;
import com.umc.product.authentication.application.port.in.command.dto.LoginByIdPwCommand;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialByEmailCommand;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialCommand;

/**
 * ID/PW 자격증명 등록/변경/로그인 UseCase.
 * <p>
 * 평문 비밀번호 검증과 인코딩 책임은 Auth 도메인이 가진다.
 * Member 도메인에는 이미 인코딩된 해시만 전달된다.
 */
public interface CredentialAuthenticationUseCase {

    /**
     * @deprecated ADR-017 에 따라 loginId 가 제거된 흐름인
     * {@link #registerCredentialByEmail(RegisterCredentialByEmailCommand)} 사용.
     */
    @Deprecated
    void registerCredential(RegisterCredentialCommand command);

    /**
     * 회원에게 이메일 기반 자격증명(비밀번호)을 최초 등록한다.
     * <p>
     * email 은 이미 회원 생성 단계에서 emailVerificationToken 으로 검증되어
     * Member.email 에 저장되어 있다.
     */
    void registerCredentialByEmail(RegisterCredentialByEmailCommand command);

    /** 현재 비밀번호 검증 후 신규 비밀번호로 교체한다. */
    void changePassword(ChangePasswordCommand command);

    /**
     * @deprecated ADR-017 에 따라 로그인 식별자가 email 로 전환됨. {@link #loginByEmail(LoginByEmailCommand)} 사용.
     */
    @Deprecated
    IdPwLoginResult loginByIdPw(LoginByIdPwCommand command);

    /**
     * 이메일/PW 로그인. 성공 시 JWT 토큰 쌍을 발급하며,
     * 해시 파라미터가 갱신 대상이면 transparent rehash 를 수행한다.
     */
    IdPwLoginResult loginByEmail(LoginByEmailCommand command);
}
