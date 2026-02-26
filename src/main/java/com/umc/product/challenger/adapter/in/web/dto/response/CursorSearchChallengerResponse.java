package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.adapter.in.web.dto.response.SearchChallengerResponse.PartCountResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.SearchChallengerResponse.SearchChallengerItemResponse;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.global.response.CursorResponse;
import java.util.List;

public record CursorSearchChallengerResponse(
    CursorResponse<SearchChallengerItemResponse> cursor,
    List<PartCountResponse> partCounts
) {
    public static CursorSearchChallengerResponse from(SearchChallengerCursorResult result) {
        List<SearchChallengerItemResponse> items = result.content().stream()
            .map(SearchChallengerItemResponse::from)
            .toList();

        return new CursorSearchChallengerResponse(
            CursorResponse.of(items, result.nextCursor(), result.hasNext()),
            PartCountResponse.from(result.partCounts())
        );
    }
}
