package com.umc.product.maintenance.application.port.in.query.dto;

public record MaintenanceStatusInfo(
    boolean inMaintenance,
    MaintenanceWindowInfo current,
    MaintenanceWindowInfo upcoming
) {
}
