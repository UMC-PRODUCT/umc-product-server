package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.MemberCredentialStatusInfo;

/**
 * 회원 로그인 수단 변경 흐름에서 local credential 상태를 lock과 함께 조회하는 Command UseCase.
 * <p>
 * 같은 회원의 OAuth 해제 요청이 동시에 진행될 때 OAuth 최소 1개 보장 규칙이 깨지지 않도록
 * member row 에 {@code PESSIMISTIC_WRITE} lock 을 잡은 상태에서 조회한다.
 */
public interface LockMemberCredentialUseCase {

    MemberCredentialStatusInfo getCredentialStatusForUpdate(Long memberId);
}
