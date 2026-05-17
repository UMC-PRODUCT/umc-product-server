package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCredentialByEmailCommand;

/**
 * Member 도메인의 이메일 기반 자격증명 변경 UseCase. ADR-017 흐름.
 * <p>
 * Authentication 도메인이 평문 비밀번호 검증/인코딩을 마친 뒤
 * "이미 인코딩된" 해시를 들고 호출한다. Member 는 평문을 보지 않는다.
 */
public interface ManageMemberCredentialUseCase {

    /**
     * 회원에 이메일 기반 자격증명을 최초로 등록한다. 이미 등록되어 있으면 도메인 예외를 던진다.
     * <p>
     * email 은 회원 생성 시점에 이미 검증되어 Member.email 에 저장되어 있다.
     */
    void registerCredentialByEmail(RegisterMemberCredentialByEmailCommand command);

    /**
     * 회원의 비밀번호 해시를 갱신한다. 자격증명이 없는 회원이면 도메인 예외를 던진다.
     */
    void changePassword(ChangeMemberPasswordCommand command);
}
