package com.umc.product.challenger.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;

public interface SearchChallengerDeactivationPort {

    Page<ChallengerDeactivationListItemInfo> search(SearchChallengerDeactivationQuery query, Pageable pageable);
}
