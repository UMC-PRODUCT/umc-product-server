package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import java.util.List;
import org.springframework.data.domain.Page;
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

    /**
     * 전체 기수/지부/학교/파트 대상으로 이름 또는 닉네임으로 챌린저를 검색합니다. (Cursor 기반)
     */
    GlobalSearchChallengerCursorResult globalCursorSearch(SearchChallengerQuery query, Long cursor, int size);

    Page<ChallengerInfo> searchV2(SearchChallengerQuery query, Pageable pageable);

    List<ChallengerInfo> searchV2(SearchChallengerQuery query, Long cursor, int size);
}
