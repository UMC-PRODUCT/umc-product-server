package com.umc.product.challenger.application.port.in.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;

public interface SearchChallengerUseCase {
    /**
     * 조건에 맞는 챌린저 목록을 조회합니다. (Offset 기반)
     */
    SearchChallengerResult offsetSearch(SearchChallengerQuery query, Pageable pageable);

    /**
     * 조건에 맞는 챌린저 목록을 조회합니다. (Cursor 기반)
     */
    SearchChallengerCursorResult cursorSearch(SearchChallengerQuery query, Long cursor, int size);

    Page<ChallengerInfo> searchV2(SearchChallengerQuery query, Pageable pageable);

    List<ChallengerInfo> searchV2(SearchChallengerQuery query, Long cursor, int size);
}
