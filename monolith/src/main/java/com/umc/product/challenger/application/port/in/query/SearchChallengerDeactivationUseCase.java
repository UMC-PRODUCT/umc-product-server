package com.umc.product.challenger.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;

public interface SearchChallengerDeactivationUseCase {

    Page<ChallengerDeactivationListItemInfo> searchDeactivations(
        Long requesterMemberId,
        SearchChallengerDeactivationQuery query,
        Pageable pageable
    );
}
