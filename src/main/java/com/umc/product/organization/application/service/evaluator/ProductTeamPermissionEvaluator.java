package com.umc.product.organization.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.organization.application.service.ProductTeamAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamPermissionEvaluator implements ResourcePermissionEvaluator {

    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.PRODUCT_TEAM;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> true;
            case WRITE -> productTeamAccessPolicy.canCreateGeneration(subjectAttributes.memberId());
            case EDIT, DELETE, MANAGE -> productTeamAccessPolicy.canManageGeneration(
                subjectAttributes.memberId(),
                resourcePermission.getResourceIdAsLong()
            );
            default -> false;
        };
    }
}
