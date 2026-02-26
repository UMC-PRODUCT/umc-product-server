package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
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
}
