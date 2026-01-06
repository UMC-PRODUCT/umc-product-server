package com.umc.product.member.application.port.in.command;

public interface ProcessOAuthLoginUseCase {
    /**
     * OAuth 로그인 처리 (회원 조회 또는 신규 가입)
     *
     * @param command OAuth 로그인 정보
     * @return 회원 ID
     */
    Long processOAuthLogin(ProcessOAuthLoginCommand command);
}
