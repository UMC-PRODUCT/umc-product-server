package com.umc.product.authorization.application.service;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.out.EvaluatePolicyPort;
import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthorizationService implements CheckPermissionUseCase {

    private final LoadChallengerRolePort loadChallengerRolePort;
    private final EvaluatePolicyPort evaluatePolicyPort;

    @Override
    public boolean check(Long memberId, ResourcePermission permission) {
        // 1. 사용자의 Role 조회
        List<ChallengerRoleType> roles = loadChallengerRolePort.findRolesByMemberId(memberId);

        // Role이 없으면 기본 CHALLENGER로 간주
        if (roles.isEmpty()) {
            roles = List.of(ChallengerRoleType.CHALLENGER);
        }

        // 2. Policy Engine으로 권한 평가
        boolean hasPermission = evaluatePolicyPort.evaluate(
                roles,
                permission.resourceType(),
                permission.resourceId(),
                permission.permission()
        );

        log.debug("Permission check - memberId: {}, roles: {}, resource: {}:{}, permission: {}, result: {}",
                memberId, roles, permission.resourceType(), permission.resourceId(),
                permission.permission(), hasPermission);

        return hasPermission;
    }

    @Override
    public void checkOrThrow(Long memberId, ResourcePermission permission) {
        if (!check(memberId, permission)) {
            log.warn("Permission denied - memberId: {}, resource: {}:{}, permission: {}",
                    memberId, permission.resourceType(), permission.resourceId(), permission.permission());

            throw new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);
        }
    }
}
