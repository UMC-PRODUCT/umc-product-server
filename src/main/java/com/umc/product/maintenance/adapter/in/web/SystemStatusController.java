package com.umc.product.maintenance.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.maintenance.adapter.in.web.dto.response.SystemStatusResponse;
import com.umc.product.maintenance.application.port.in.query.GetMaintenanceStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "System | 점검 상태", description = "앱 진입 시 필요한 시스템 점검 상태를 제공합니다.")
public class SystemStatusController {

    private final GetMaintenanceStatusUseCase getMaintenanceStatusUseCase;

    @GetMapping("/status")
    @Public
    @Operation(
        operationId = "SYSTEM-001",
        summary = "시스템 점검 상태 조회",
        description = "현재 점검 활성 여부와 다음 예약 점검 정보를 반환합니다. 점검 중에도 항상 통과합니다."
    )
    public SystemStatusResponse getStatus() {
        return SystemStatusResponse.from(getMaintenanceStatusUseCase.getStatus());
    }
}
