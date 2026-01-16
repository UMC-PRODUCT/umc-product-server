package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.trophy.TrophyInfo;

public record TrophyResponse(
        Long trophyId,
        Integer week,
        String challengerName,
        String school,
        String part,
        String title,
        String content,
        String url
) {
    public static TrophyResponse from(TrophyInfo info) {
        return new TrophyResponse(
                info.trophyId(),
                info.week(),
                info.challengerName(),
                info.school(),
                info.part(),
                info.title(),
                info.content(),
                info.url()
        );
    }
}
