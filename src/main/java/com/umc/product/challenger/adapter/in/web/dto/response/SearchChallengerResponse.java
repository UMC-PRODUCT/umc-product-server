package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.SearchChallengerResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.PageResponse;
import java.util.ArrayList;
import java.util.List;

public record SearchChallengerResponse(
        PageResponse<SearchChallengerItemResponse> page,
        List<PartCountResponse> partCounts
) {
    public static SearchChallengerResponse from(SearchChallengerResult result) {
        return new SearchChallengerResponse(
                PageResponse.of(result.page(), SearchChallengerItemResponse::from),
                PartCountResponse.from(result.partCounts())
        );
    }

    public record SearchChallengerItemResponse(
            Long challengerId,
            Long memberId,
            Long gisuId,
            ChallengerPart part,
            String name,
            String nickname,
            Double pointSum,
            String profileImageLink
    ) {
        public static SearchChallengerItemResponse from(SearchChallengerItemInfo info) {
            return new SearchChallengerItemResponse(
                    info.challengerId(),
                    info.memberId(),
                    info.gisuId(),
                    info.part(),
                    info.name(),
                    info.nickname(),
                    info.pointSum(),
                    info.profileImageLink()
            );
        }
    }

    public record PartCountResponse(
            ChallengerPart part,
            long count
    ) {
        private static final List<ChallengerPart> ORDER = List.of(
                ChallengerPart.PLAN,
                ChallengerPart.DESIGN,
                ChallengerPart.WEB,
                ChallengerPart.ANDROID,
                ChallengerPart.IOS,
                ChallengerPart.NODEJS,
                ChallengerPart.SPRINGBOOT
        );

        public static List<PartCountResponse> from(java.util.Map<ChallengerPart, Long> counts) {
            List<PartCountResponse> responses = new ArrayList<>(ORDER.size());
            for (ChallengerPart part : ORDER) {
                responses.add(new PartCountResponse(part, counts.getOrDefault(part, 0L)));
            }
            return responses;
        }
    }
}
