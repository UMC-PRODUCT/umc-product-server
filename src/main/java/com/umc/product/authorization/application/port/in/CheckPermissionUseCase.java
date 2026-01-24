package com.umc.product.authorization.application.port.in;

import com.umc.product.authorization.domain.ResourcePermission;

/**
 * 권한 체크 UseCase
 */
public interface CheckPermissionUseCase {

    /**
     * 사용자가 특정 리소스에 대한 권한이 있는지 확인
     *
     * @param memberId   사용자 ID
     * @param permission 확인할 권한
     * @return 권한 있으면 true, 없으면 false
     */
    boolean check(Long memberId, ResourcePermission permission);

    /**
     * 권한이 없으면 예외를 발생시키는 메서드
     *
     * @param memberId   사용자 ID
     * @param permission 확인할 권한
     * @throws com.umc.product.authorization.domain.exception.AuthorizationDomainException 권한이 없는 경우
     */
    void checkOrThrow(Long memberId, ResourcePermission permission);
}
