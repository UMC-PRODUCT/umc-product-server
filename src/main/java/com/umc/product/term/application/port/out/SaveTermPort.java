package com.umc.product.term.application.port.out;

import com.umc.product.term.domain.Term;

public interface SaveTermPort {
    /**
     * 약관을 저장합니다.
     */
    Term save(Term term);

    /**
     * 약관을 삭제합니다.
     */
    void delete(Term term);
}
