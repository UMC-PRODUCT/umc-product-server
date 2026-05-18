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
            window.getTargetDomains() == null
                ? EnumSet.noneOf(MaintenanceDomain.class)
                : EnumSet.copyOf(window.getTargetDomains()),
            window.getStartAt(),
            window.getEndAt(),
            window.getTitle(),
            window.getMessage()
        );
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
