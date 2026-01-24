package com.umc.product.authorization.adapter.out.policy;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;

/**
 * 리소스별 권한 평가 Strategy
 *
 * <p>각 리소스 타입(Curriculum, Schedule, Notice 등)별로 세밀한 권한 제어가 필요한 경우 구현합니다.</p>
 */
public interface ResourcePermissionEvaluator {

    /**
     * 이 Evaluator가 처리할 수 있는 ResourceType
     */
    ResourceType supportedResourceType();

    /**
     * 특정 리소스에 대한 권한 평가
     *
     * @param roles      사용자의 Role 리스트
     * @param resourceId 리소스 ID (null이면 타입 전체에 대한 권한 체크)
     * @param permission 권한 타입
     * @return 권한 있으면 true
     */
    boolean evaluate(
            List<ChallengerRoleType> roles,
            String resourceId,
            PermissionType permission
    );
}
