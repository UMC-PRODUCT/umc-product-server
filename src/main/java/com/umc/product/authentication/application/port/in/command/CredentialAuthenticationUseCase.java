package com.umc.product.authentication.application.port.in.command;

import com.umc.product.authentication.application.port.in.command.dto.ChangePasswordCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LoginByIdPwCommand;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialCommand;

/**
 * ID/PW 자격증명 등록/변경/로그인 UseCase.
 * <p>
 * 평문 비밀번호 검증과 인코딩 책임은 Auth 도메인이 가진다.
 * Member 도메인에는 이미 인코딩된 해시만 전달된다.
 */
public interface CredentialAuthenticationUseCase {

    /** 회원에게 ID/PW 자격증명을 최초 등록한다. */
    void registerCredential(RegisterCredentialCommand command);

    /** 현재 비밀번호 검증 후 신규 비밀번호로 교체한다. */
    void changePassword(ChangePasswordCommand command);

    /**
     * ID/PW 로그인. 성공 시 JWT 토큰 쌍을 발급하며,
     * 해시 파라미터가 갱신 대상이면 transparent rehash 를 수행한다.
     */
    IdPwLoginResult loginByIdPw(LoginByIdPwCommand command);
}
