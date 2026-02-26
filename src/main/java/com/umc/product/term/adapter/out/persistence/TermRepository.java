package com.umc.product.term.adapter.out.persistence;

import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

    /**
     * IN 절을 사용하여 전달받은 타입들에 해당하는 활성 약관을 한 번에 조회합니다.
     */
    List<Term> findAllByTypeInAndActiveIsTrue(List<TermType> types);
}
