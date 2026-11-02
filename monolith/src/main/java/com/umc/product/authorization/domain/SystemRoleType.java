package com.umc.product.authorization.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * 회원 단위로 부여되는 시스템 권한입니다.
 * <p>
 * 저장 위치는 member 도메인이 될 수 있지만, 권한 정책의 의미는 authorization 도메인에서 해석합니다.
 */
public enum SystemRoleType {
    SUPER_ADMIN,
    UMC_PRODUCT;

    /**
     * 처음 보는 값은 권한을 주지 않는 쪽으로 빈 값을 돌려줍니다. 배포 순서가 어긋나거나 롤백되어 DB에만 있는 역할이 있어도 권한 검사 전체가 실패하지 않게 하기 위함입니다.
     */
    public static Optional<SystemRoleType> find(String roleType) {
        return Arrays.stream(values())
            .filter(type -> type.name().equals(roleType))
            .findFirst();
    }
}
