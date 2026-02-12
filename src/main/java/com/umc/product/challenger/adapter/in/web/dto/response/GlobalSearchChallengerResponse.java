package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerItemInfo;
import com.umc.product.global.response.CursorResponse;

public record GlobalSearchChallengerResponse(
        CursorResponse<GlobalSearchChallengerItemResponse> cursor
) {
    public static GlobalSearchChallengerResponse from(GlobalSearchChallengerCursorResult result) {
        var items = result.content().stream()
                .map(GlobalSearchChallengerItemResponse::from)
                .toList();

        return new GlobalSearchChallengerResponse(
                CursorResponse.of(items, result.nextCursor(), result.hasNext())
        );
    }

    public record GlobalSearchChallengerItemResponse(
            Long challengerId,
            String nickname,
            String name,
            String schoolName,
            Long generation,
            String profileImageLink
    ) {
        public static GlobalSearchChallengerItemResponse from(GlobalSearchChallengerItemInfo info) {
            return new GlobalSearchChallengerItemResponse(
                    info.challengerId(),
                    info.nickname(),
                    info.name(),
                    info.schoolName(),
                    info.generation(),
                    info.profileImageLink()
            );
        }
    }
}
