package com.umc.product.terms.application.port.out;

import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;

public interface LoadTermsPort {
    /**
     * ID로 약관을 조회합니다.
     */
    Optional<Terms> findById(Long id);

    /**
     * 특정 타입의 활성화된 약관을 조회합니다. 여러 개일 경우 가장 최신 것을 반환합니다.
     */
    Optional<Terms> findActiveByType(TermsType type);

    /**
     * ID로 약관이 존재하는지 확인합니다.
     */
    boolean existsById(Long id);

    /**
     * 전달받은 타입들에 해당하는 활성 약관을 한 번에 조회합니다.
     */
    List<Terms> findAllActiveByTypes(List<TermsType> types);

    /**
     * 현재 활성화된 필수 약관 목록을 조회합니다.
     */
    List<Terms> findAllActiveRequired();
}
