package com.umc.product.authorization.application.port.out;

import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;

/**
 * 특정 리소스 타입에 대한 권한을 평가하는 Evaluator 입니다.
 * <p>
 * 각 Evaluator가 처리할 수 있는 리소스 유형을 명시하여야 하며, 현재는 주체 속성에 대해서 해당 리소스에 대한 권한이 있는지를 평가하게 됩니다.
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
        SubjectAttributes subjectAttributes,
        ResourcePermission resourcePermission
    );
}
