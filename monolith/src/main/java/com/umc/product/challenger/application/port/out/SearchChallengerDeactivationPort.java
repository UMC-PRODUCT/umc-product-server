package com.umc.product.challenger.application.port.out;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationExportRow;

public interface SearchChallengerDeactivationPort {

    Page<ChallengerDeactivationListItemInfo> search(SearchChallengerDeactivationQuery query, Pageable pageable);

    List<ChallengerDeactivationExportRow> listExportRows(
        SearchChallengerDeactivationQuery scope,
        ExportChallengerDeactivationQuery query,
        Instant startInclusive,
        Instant endExclusive,
        int limit
    );
}
