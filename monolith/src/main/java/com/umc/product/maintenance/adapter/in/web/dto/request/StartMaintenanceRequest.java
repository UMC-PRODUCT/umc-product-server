package com.umc.product.maintenance.adapter.in.web.dto.request;

import com.umc.product.maintenance.application.port.in.command.dto.StartMaintenanceCommand;
import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

@Schema(description = "점검 시작 요청")
public record StartMaintenanceRequest(
    @Schema(description = "점검 범위. FULL 또는 PER_DOMAIN", example = "FULL")
    @NotNull
    MaintenanceScope scope,

    @Schema(description = "PER_DOMAIN 점검 시 대상 도메인 (FULL 이면 null/empty 허용)")
    Set<MaintenanceDomain> targetDomains,

    @Schema(description = "점검 시작 시각 (UTC). 즉시 시작이면 현재 시각")
    @NotNull
    Instant startAt,

    @Schema(description = "점검 종료 시각 (UTC). 필수")
    @NotNull
    Instant endAt,

    @Schema(description = "점검 제목", example = "정기 점검")
    @NotBlank
    @Size(max = 255)
    String title,

    @Schema(description = "점검 안내 메시지")
    @NotBlank
    @Size(max = 1000)
    String message
) {

    public StartMaintenanceCommand toCommand(Long createdBy) {
        return new StartMaintenanceCommand(
            scope,
            targetDomains,
            startAt,
            endAt,
            title,
            message,
            createdBy
        );
    }
}
