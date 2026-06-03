package com.umc.product.analytics.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.MemberRoleType;

public record AdminAnalyticsScope(
    AdminAnalyticsScopeType type,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart responsiblePart,
    MemberRoleType memberRoleType,
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
        return new AdminAnalyticsScope(type, gisuId, chapterId, schoolId, responsiblePart, null, roleType);
    }

    public static AdminAnalyticsScope memberAdmin(Long gisuId, Long chapterId, Long schoolId,
                                                  ChallengerPart responsiblePart) {
        return new AdminAnalyticsScope(
            AdminAnalyticsScopeType.CENTRAL,
            gisuId,
            chapterId,
            schoolId,
            responsiblePart,
            MemberRoleType.ADMIN,
            null
        );
    }

    public boolean isCentralScope() {
        return type == AdminAnalyticsScopeType.CENTRAL;
    }
}
