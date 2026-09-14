package com.umc.product.maintenance.application.port.in.command.dto;

import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;
import java.time.Instant;
import java.util.Set;

public record StartMaintenanceCommand(
    MaintenanceScope scope,
    Set<MaintenanceDomain> targetDomains,
    Instant startAt,
    Instant endAt,
    String title,
    String message,
    Long createdBy
) {
}
