package com.umc.product.analytics.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

public record AdminAnalyticsScope(
    AdminAnalyticsScopeType type,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart responsiblePart,
    ChallengerRoleType roleType
) {

    public static AdminAnalyticsScope of(
        AdminAnalyticsScopeType type,
        Long gisuId,
        Long chapterId,
        Long schoolId,
        ChallengerPart responsiblePart,
        ChallengerRoleType roleType
    ) {
        return new AdminAnalyticsScope(type, gisuId, chapterId, schoolId, responsiblePart, roleType);
    }

    public boolean isCentralScope() {
        return type == AdminAnalyticsScopeType.CENTRAL;
    }
}
