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
     * loginId 로 자격증명을 조회한다. 존재하지 않으면 {@link Optional#empty()}.
     * <p>
     * "사용자 없음" 과 "비밀번호 불일치" 를 외부에 구분 노출하지 않기 위해
     * 여기서는 예외를 던지지 않고 빈 Optional 을 반환한다.
     */
    Optional<MemberCredentialInfo> findCredentialByLoginId(String loginId);

    /**
     * loginId 가 이미 사용 중인지 확인한다. (회원가입 / 자격증명 등록 시 중복 방지)
     */
    boolean existsByLoginId(String loginId);
}
