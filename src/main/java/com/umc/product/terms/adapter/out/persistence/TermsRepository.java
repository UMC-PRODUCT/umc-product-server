package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    /**
     * IN 절을 사용하여 전달받은 타입들에 해당하는 활성 약관을 한 번에 조회합니다.
     */
    List<Terms> findAllByTypeInAndActiveIsTrue(List<TermsType> types);
}
