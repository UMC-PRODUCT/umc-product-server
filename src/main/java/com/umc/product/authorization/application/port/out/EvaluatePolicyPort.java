package com.umc.product.authorization.application.port.out;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;

/**
 * 권한 정책을 평가하는 Port (Casbin 같은 Policy Engine)
 */
public interface EvaluatePolicyPort {

    /**
     * Role이 Resource에 대한 Permission을 가지고 있는지 평가함
     *
     * @param roles        사용자의 Role 리스트
     * @param resourceType 리소스 타입 (CURRICULUM, SCHEDULE 등)
     * @param resourceId   리소스 ID (optional)
     * @param permission   권한 타입
     * @return 권한 있으면 true
     */
    boolean evaluate(
            List<ChallengerRoleType> roles,
            ResourceType resourceType,
            String resourceId,
            PermissionType permission
    );
}
