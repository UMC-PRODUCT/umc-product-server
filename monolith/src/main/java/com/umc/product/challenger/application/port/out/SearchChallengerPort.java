package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchBundle;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchChallengerPort {
    Page<Challenger> search(SearchChallengerQuery query, Pageable pageable);

    List<Challenger> cursorSearch(SearchChallengerQuery query, Long cursor, int size);

    Map<ChallengerPart, Long> countByPart(SearchChallengerQuery query);

    Map<Long, Double> sumPointsByChallengerIds(Set<Long> challengerIds);

    /**
     * 커서 기반 검색 + 파트별 카운트를 하나의 호출로 수행합니다.
     * <p>
     * 검색 조건을 한 번만 생성하고, member/school을 JOIN하여 프로필 정보를 함께 조회합니다.
     */
    ChallengerSearchBundle cursorSearchWithCounts(SearchChallengerQuery query, Long cursor, int size);

    /**
     * 오프셋 기반 검색 + 파트별 카운트를 하나의 호출로 수행합니다.
     */
    ChallengerSearchBundle pagingSearchWithCounts(SearchChallengerQuery query, Pageable pageable);
}
