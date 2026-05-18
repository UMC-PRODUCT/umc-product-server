package com.umc.product.maintenance.adapter.in.web.dto.response;

import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceWindowInfo;
import com.umc.product.maintenance.domain.MaintenanceDomain;
import com.umc.product.maintenance.domain.MaintenanceScope;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;

@Schema(description = "점검 윈도우 정보")
public record MaintenanceWindowResponse(
    @Schema(description = "점검 윈도우 ID")
    Long id,

    @Schema(description = "점검 범위", example = "FULL")
    MaintenanceScope scope,

    @Schema(description = "PER_DOMAIN 점검 대상 도메인 (FULL 이면 빈 집합)")
    Set<MaintenanceDomain> targetDomains,

    @Schema(description = "점검 시작 시각 (UTC)")
    Instant startAt,

    @Schema(description = "점검 종료 시각 (UTC)")
    Instant endAt,

    @Schema(description = "점검 제목", example = "정기 점검")
    String title,

    @Schema(description = "점검 안내 메시지")
    String message,

    @Schema(description = "강제 종료 시각. 종료되지 않았으면 null")
    Instant forcedEndedAt,

    @Schema(description = "강제 종료를 수행한 운영자 memberId. 강제 종료되지 않았으면 null")
    Long forcedEndedBy,

    @Schema(description = "생성자 memberId")
    Long createdBy,

    @Schema(description = "생성 시각")
    Instant createdAt
) {

    public static MaintenanceWindowResponse from(MaintenanceWindowInfo info) {
        return new MaintenanceWindowResponse(
            info.id(),
            info.scope(),
            info.targetDomains(),
            info.startAt(),
            info.endAt(),
            info.title(),
            info.message(),
            info.forcedEndedAt(),
            info.forcedEndedBy(),
            info.createdBy(),
            info.createdAt()
        );
    }
}
