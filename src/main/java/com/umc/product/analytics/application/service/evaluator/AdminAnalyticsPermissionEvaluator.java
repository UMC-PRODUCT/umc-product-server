package com.umc.product.analytics.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import org.springframework.stereotype.Component;

@Component
public class AdminAnalyticsPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ANALYTICS;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (resourcePermission.permission() != PermissionType.READ) {
            return false;
        }

        return subjectAttributes.roleAttributes().stream()
            .map(RoleAttribute::roleType)
            .anyMatch(roleType -> roleType.isSuperAdmin()
                || roleType.isAtLeastCentralMember()
                || roleType == ChallengerRoleType.CHAPTER_PRESIDENT
                || roleType.isAtLeastSchoolAdmin());
    }
}
