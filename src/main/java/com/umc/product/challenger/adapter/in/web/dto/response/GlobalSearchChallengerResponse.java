package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerItemInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.CursorResponse;

/**
 * Challenger Global Search의 응답을 위한 DTO
 */
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
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        Long gisu,
        ChallengerPart part,
        String profileImageLink
    ) {
        public static GlobalSearchChallengerItemResponse from(GlobalSearchChallengerItemInfo info) {
            return new GlobalSearchChallengerItemResponse(
                info.memberId(),
                info.nickname(),
                info.name(),
                info.schoolName(),
                info.gisu(),
                info.part(),
                info.profileImageLink()
            );
        }
    }
}
