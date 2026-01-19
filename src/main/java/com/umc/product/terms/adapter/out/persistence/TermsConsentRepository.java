package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsConsentRepository extends JpaRepository<TermsConsent, Long> {

    /**
     * 회원 ID로 동의한 약관 목록을 조회합니다.
     */
    List<TermsConsent> findByMemberId(Long memberId);

    /**
     * 회원 ID와 약관 타입으로 동의 정보를 조회합니다.
     */
    Optional<TermsConsent> findByMemberIdAndTermType(Long memberId, TermsType termType);

    /**
     * 회원이 특정 타입의 약관에 동의했는지 확인합니다.
     */
    boolean existsByMemberIdAndTermType(Long memberId, TermsType termType);
}
