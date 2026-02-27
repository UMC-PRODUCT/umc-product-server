package com.umc.product.term.adapter.out.persistence;

import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.enums.TermType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermConsentRepository extends JpaRepository<TermConsent, Long> {

    /**
     * 회원 ID로 동의한 약관 목록을 조회합니다.
     */
    List<TermConsent> findByMemberId(Long memberId);

    /**
     * 회원 ID와 약관 타입으로 동의 정보를 조회합니다.
     */
    Optional<TermConsent> findByMemberIdAndTermType(Long memberId, TermType termType);

    /**
     * 회원이 특정 타입의 약관에 동의했는지 확인합니다.
     */
    boolean existsByMemberIdAndTermType(Long memberId, TermType termType);
}
