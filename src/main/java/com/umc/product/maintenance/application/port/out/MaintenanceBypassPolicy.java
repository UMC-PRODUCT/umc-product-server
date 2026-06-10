package com.umc.product.maintenance.application.port.out;

/**
 * 점검 중 차단을 우회할 수 있는 사용자를 결정하는 정책.
 * <p>
 * 현재 구현: {@code MemberRoleType.ADMIN} 보유자.
 * 추후 {@code Member.role} 컬럼이 도입되면 본 인터페이스의 구현체만 교체하면 된다.
 */
public interface MaintenanceBypassPolicy {

    boolean shouldBypass(Long memberId);
}
