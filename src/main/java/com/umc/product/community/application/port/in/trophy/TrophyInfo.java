package com.umc.product.community.application.port.in.trophy;

import com.umc.product.community.domain.Trophy;

public record TrophyInfo(
        Long trophyId,
        Integer week,
        String challengerName,
        String school,
        String part,
        String title,
        String content,
        String url
) {
    // TODO: challengerName, school, part는 Challenger/Organization 도메인에서 조회 필요
    public static TrophyInfo from(Trophy trophy) {
        return new TrophyInfo(
                trophy.getTrophyId() != null ? trophy.getTrophyId().id() : null,
                trophy.getWeek(),
                null,
                null,
                null,
                trophy.getTitle(),
                trophy.getContent(),
                trophy.getUrl()
        );
    }
}

