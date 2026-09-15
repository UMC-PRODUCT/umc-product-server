package com.umc.product.maintenance.application.port.in.command.dto;

import java.time.Instant;
import java.util.Set;

import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;

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
