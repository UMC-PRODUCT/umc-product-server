package com.umc.product.project.application.port.in.query.dto;

public record ProjectPermissionCapabilityInfo(
    boolean allowed,
    String reasonCode,
    String reason
) {

    public static ProjectPermissionCapabilityInfo allow() {
        return new ProjectPermissionCapabilityInfo(true, null, null);
    }

    public static ProjectPermissionCapabilityInfo denied(ProjectPermissionReason reason) {
        return denied(reason, reason.getMessage());
    }

    public static ProjectPermissionCapabilityInfo denied(ProjectPermissionReason reason, String message) {
        return new ProjectPermissionCapabilityInfo(false, reason.name(), message);
    }
}
