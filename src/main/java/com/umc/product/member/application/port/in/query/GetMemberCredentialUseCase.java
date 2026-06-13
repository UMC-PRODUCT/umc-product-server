package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialStatusInfo;
import java.util.Optional;

/**
 * Member 도메인의 이메일 기반 자격증명 조회 UseCase. ADR-017 흐름.
 * <p>
 * Authentication 도메인이 cross-domain 으로 호출하는 진입점이다.
 * Auth 도메인은 Member 의 Port out / Repository 를 직접 사용하지 않는다.
 */
public interface GetMemberCredentialUseCase {

    /**
     * email 로 자격증명을 조회한다. 존재하지 않거나 비밀번호가 등록되지 않은 회원은 {@link Optional#empty()}.
     * <p>
     * "사용자 없음" 과 "비밀번호 불일치" 를 외부에 구분 노출하지 않기 위해
     * 여기서는 예외를 던지지 않고 빈 Optional 을 반환한다.
     */
    Optional<MemberCredentialInfo> findCredentialByEmail(String email);

    /**
     * memberId 로 자격증명을 조회한다. 자격증명이 등록되지 않은 회원은 {@link Optional#empty()}.
     * <p>
     * 비밀번호 변경 등 인증된 사용자 흐름에서 사용한다.
     */
    Optional<MemberCredentialInfo> findCredentialByMemberId(Long memberId);

    /**
     * memberId 로 local credential 등록 여부를 조회한다.
     * <p>
     * 같은 회원의 로그인 수단 변경이 동시에 진행될 때 OAuth 최소 1개 보장 규칙이 깨지지 않도록
     * 회원 row 에 {@code PESSIMISTIC_WRITE} lock 을 잡은 상태에서 조회한다.
     */
    MemberCredentialStatusInfo getCredentialStatusForUpdate(Long memberId);

    /**
     * email 이 이미 사용 중인지 확인한다. (회원가입 / 자격증명 등록 시 중복 방지)
     */
    boolean existsByEmail(String email);
}
