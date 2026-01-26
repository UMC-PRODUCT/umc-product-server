package com.umc.product.authorization.adapter.out.policy.evaluator;

import com.umc.product.authorization.adapter.out.policy.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.exception.NotImplementedException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Curriculum(커리큘럼) 리소스에 대한 권한 평가
 */
@Component
@Slf4j
public class CurriculumPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.CURRICULUM;
    }

    @Override
    public boolean evaluate(List<ChallengerRoleType> roles, String resourceId, PermissionType permission) {
        throw new NotImplementedException(this.getClass().getName() + " Permission Evaluator를 구현해주세요.");
    }
}
