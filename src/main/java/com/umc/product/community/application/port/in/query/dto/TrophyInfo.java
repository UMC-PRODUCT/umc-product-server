package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.community.domain.Trophy;

public record TrophyInfo(
    Long trophyId,
    Long challengerId,
    Integer week,
    String challengerName,
    String challengerProfileImage,
    String school,
    String part,
    String title,
    String content,
    String url
) {
    public static TrophyInfo from(Trophy trophy) {
        return new TrophyInfo(
            trophy.getTrophyId() != null ? trophy.getTrophyId().id() : null,
            trophy.getChallengerId() != null ? trophy.getChallengerId().id() : null,
            trophy.getWeek(),
            null,
            null,
            null,
            null,
            trophy.getTitle(),
            trophy.getContent(),
            trophy.getUrl()
        );
    }

    public static TrophyInfo of(Trophy trophy, String challengerName, String challengerProfileImage, String school,
                                String part) {
        return new TrophyInfo(
            trophy.getTrophyId() != null ? trophy.getTrophyId().id() : null,
            trophy.getChallengerId() != null ? trophy.getChallengerId().id() : null,
            trophy.getWeek(),
            challengerName,
            challengerProfileImage,
            school,
            part,
            trophy.getTitle(),
            trophy.getContent(),
            trophy.getUrl()
        );
    }
}

