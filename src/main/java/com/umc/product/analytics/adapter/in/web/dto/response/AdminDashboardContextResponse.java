package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardContextInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScopeType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.MemberRoleType;

public record AdminDashboardContextResponse(
    MemberRoleType memberRoleType,
    ChallengerRoleType roleType,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart responsiblePart,
    AdminAnalyticsScopeType scopeType
) {

    public static AdminDashboardContextResponse from(AdminDashboardContextInfo info) {
        return new AdminDashboardContextResponse(
            info.memberRoleType(),
            info.roleType(),
            info.gisuId(),
            info.chapterId(),
            info.schoolId(),
            info.responsiblePart(),
            info.scopeType()
        );
    }
}
