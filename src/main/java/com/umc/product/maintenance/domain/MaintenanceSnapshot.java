package com.umc.product.maintenance.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * 필터에서 매 요청마다 참조하는 불변 캐시 스냅샷.
 * 활성 윈도우가 없으면 {@link #none()} 을 사용한다.
 */
public record MaintenanceSnapshot(
    boolean active,
    Long activeWindowId,
    MaintenanceScope scope,
    Set<MaintenanceDomain> targetDomains,
    Instant startAt,
    Instant endAt,
    String title,
    String message
) {

    private static final MaintenanceSnapshot NONE = new MaintenanceSnapshot(
        false, null, null, EnumSet.noneOf(MaintenanceDomain.class), null, null, null, null
    );

    public static MaintenanceSnapshot none() {
        return NONE;
    }

    public static MaintenanceSnapshot from(MaintenanceWindow window) {
        return new MaintenanceSnapshot(
            true,
            window.getId(),
            window.getScope(),
            copyDomains(window.getTargetDomains()),
            window.getStartAt(),
            window.getEndAt(),
            window.getTitle(),
            window.getMessage()
        );
    }

    /**
     * Hibernate PersistentSet 처럼 비어 있을 수 있는 컬렉션에 대해
     * {@link EnumSet#copyOf(java.util.Collection)} 가 던지는 {@link IllegalArgumentException} 을 방어한다.
     */
    private static Set<MaintenanceDomain> copyDomains(Set<MaintenanceDomain> source) {
        if (source == null || source.isEmpty()) {
            return EnumSet.noneOf(MaintenanceDomain.class);
        }
        return EnumSet.copyOf(source);
    }

    public boolean blocks(String requestUri) {
        if (!active) {
            return false;
        }
        if (scope == MaintenanceScope.FULL) {
            return true;
        }
        return MaintenanceDomain.fromUri(requestUri)
            .map(targetDomains::contains)
            .orElse(false);
    }
}
