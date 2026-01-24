package com.umc.product.authorization.adapter.out.policy;

import com.umc.product.authorization.application.port.out.EvaluatePolicyPort;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 권한 평가 Orchestrator
 *
 * <p>ResourceType별 Strategy를 사용하여 권한을 평가합니다.</p>
 * <p>Strategy가 등록되지 않은 ResourceType은 기본 권한 매트릭스로 평가합니다.</p>
 */
@Component
@Slf4j
public class SimplePolicyEvaluator implements EvaluatePolicyPort {

    private final Map<ResourceType, ResourcePermissionEvaluator> evaluators;

    /**
     * ResourcePermissionEvaluator에 대한 생성자 주입
     * <p>
     * 셍성자에서 List를 Map으로 변환하기 위해서 Lombok을 사용하지 않았음.
     */
    public SimplePolicyEvaluator(List<ResourcePermissionEvaluator> evaluatorList) {
        this.evaluators = evaluatorList.stream()
                .collect(Collectors.toMap(
                        ResourcePermissionEvaluator::supportedResourceType,
                        Function.identity()
                ));

        log.info("등록된 ResourcePermissionEvaluator: {}", evaluators.keySet());
    }

    @Override
    public boolean evaluate(
            List<ChallengerRoleType> roles,
            ResourceType resourceType,
            String resourceId,
            PermissionType permission
    ) {
        log.debug("Policy 평가 시작 - resourceType: {}, resourceId: {}, roles: {}, permission: {}",
                resourceType, resourceId, roles, permission);

        // 1. ResourceType별 Strategy가 있으면 위임
        ResourcePermissionEvaluator evaluator = evaluators.get(resourceType);

        if (evaluator != null) {
            boolean result = evaluator.evaluate(roles, resourceId, permission);
            log.debug("Policy 평가 (Strategy) - resourceType: {}, resourceId: {}, roles: {}, permission: {}, result: {}",
                    resourceType, resourceId, roles, permission, result);
            return result;
        }

        // TODO: 강1와 경운의 생각에는 fallback으로 가면 안될 것 같음. (evaluator가 존재하지 않는 것이기 때문에)
        throw new AuthorizationDomainException(AuthorizationErrorCode.NO_EVALUATOR_MATCHING_RESOURCE_TYPE,
                "Evaluator for Resource Type [" + resourceType + "] not found.");

        // 2. Strategy가 없으면 기본 권한 매트릭스로 평가
//        boolean result = DefaultPermissionEvaluator.hasPermission(roles, permission);
//        log.debug("Policy 평가 (Default) - resourceType: {}, resourceId: {}, roles: {}, permission: {}, result: {}",
//                resourceType, resourceId, roles, permission, result);
//        return result;
    }
}
