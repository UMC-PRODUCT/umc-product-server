package com.umc.product.maintenance.application.port.in.query.dto;

import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;
import com.umc.product.maintenance.domain.MaintenanceWindow;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

public record MaintenanceWindowInfo(
    Long id,
    MaintenanceScope scope,
    Set<MaintenanceDomain> targetDomains,
    Instant startAt,
    Instant endAt,
    String title,
    String message,
    Instant forcedEndedAt,
    Long forcedEndedBy,
    Long createdBy,
    Instant createdAt
) {

    public static MaintenanceWindowInfo from(MaintenanceWindow window) {
        return new MaintenanceWindowInfo(
            window.getId(),
            window.getScope(),
            copyDomains(window.getTargetDomains()),
            window.getStartAt(),
            window.getEndAt(),
            window.getTitle(),
            window.getMessage(),
            window.getForcedEndedAt(),
            window.getForcedEndedBy(),
            window.getCreatedBy(),
            window.getCreatedAt()
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
}
