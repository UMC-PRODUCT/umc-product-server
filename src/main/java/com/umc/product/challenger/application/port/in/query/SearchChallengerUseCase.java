package com.umc.product.challenger.application.port.in.query;

import org.springframework.data.domain.Pageable;

public interface SearchChallengerUseCase {

    /**
     * 조건에 맞는 챌린저 목록을 조회합니다.
     */
    SearchChallengerResult search(SearchChallengerQuery query, Pageable pageable);

}
