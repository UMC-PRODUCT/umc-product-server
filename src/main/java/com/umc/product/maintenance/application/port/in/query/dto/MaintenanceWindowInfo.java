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
    Long createdBy,
    Instant createdAt
) {

    public static MaintenanceWindowInfo from(MaintenanceWindow window) {
        return new MaintenanceWindowInfo(
            window.getId(),
            window.getScope(),
            window.getTargetDomains() == null
                ? EnumSet.noneOf(MaintenanceDomain.class)
                : EnumSet.copyOf(window.getTargetDomains()),
            window.getStartAt(),
            window.getEndAt(),
            window.getTitle(),
            window.getMessage(),
            window.getForcedEndedAt(),
            window.getCreatedBy(),
            window.getCreatedAt()
        );
    }
}
