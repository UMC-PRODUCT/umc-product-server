package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    /**
     * 특정 타입의 활성화된 약관을 최신순으로 조회합니다.
     */
    @Query("SELECT t FROM Terms t WHERE t.type = :type AND t.active = true ORDER BY t.effectiveDate DESC LIMIT 1")
    Optional<Terms> findActiveByType(@Param("type") TermsType type);

    /**
     * IN 절을 사용하여 전달받은 타입들에 해당하는 활성 약관을 한 번에 조회합니다.
     */
    List<Terms> findAllByTypeInAndActiveIsTrue(List<TermsType> types);
}
