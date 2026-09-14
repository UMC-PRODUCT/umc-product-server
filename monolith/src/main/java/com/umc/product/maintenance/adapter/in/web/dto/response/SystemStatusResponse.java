package com.umc.product.maintenance.adapter.in.web.dto.response;

import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceStatusInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시스템 점검 상태")
public record SystemStatusResponse(
    @Schema(description = "현재 점검 중 여부")
    boolean inMaintenance,

    @Schema(description = "현재 활성 점검 윈도우. 없으면 null")
    MaintenanceWindowResponse current,

    @Schema(description = "다음 예약 점검 윈도우. 없으면 null")
    MaintenanceWindowResponse upcoming
) {

    public static SystemStatusResponse from(MaintenanceStatusInfo info) {
        return new SystemStatusResponse(
            info.inMaintenance(),
            info.current() == null ? null : MaintenanceWindowResponse.from(info.current()),
            info.upcoming() == null ? null : MaintenanceWindowResponse.from(info.upcoming())
        );
    }
}
