package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import java.util.Optional;

/**
 * Member 도메인의 ID/PW 자격증명 조회 UseCase.
 * <p>
 * Authentication 도메인이 cross-domain 으로 호출하는 진입점이다.
 * Auth 도메인은 Member 의 Port out / Repository 를 직접 사용하지 않는다.
 */
public interface GetMemberCredentialUseCase {

    /**
     * @deprecated ADR-017 에 따라 로그인 식별자가 email 로 전환됨.
     * {@link #findCredentialByEmail(String)} 사용.
     */
    @Deprecated
    Optional<MemberCredentialInfo> findCredentialByLoginId(String loginId);

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
     * @deprecated ADR-017 에 따라 로그인 식별자가 email 로 전환됨.
     * {@link #existsByEmail(String)} 사용.
     */
    @Deprecated
    boolean existsByLoginId(String loginId);

    /**
     * email 이 이미 사용 중인지 확인한다. (회원가입 / 자격증명 등록 시 중복 방지)
     */
    boolean existsByEmail(String email);
}
