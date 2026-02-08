package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import org.springframework.data.domain.Pageable;

public interface SearchChallengerUseCase {

    /**
     * 조건에 맞는 챌린저 목록을 조회합니다. (Offset 기반)
     */
    SearchChallengerResult search(SearchChallengerQuery query, Pageable pageable);

    /**
     * 조건에 맞는 챌린저 목록을 조회합니다. (Cursor 기반)
     */
    SearchChallengerCursorResult cursorSearch(SearchChallengerQuery query, Long cursor, int size);

}
