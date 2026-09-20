package com.umc.product.maintenance.application.port.in.query;

import java.util.List;

import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceStatusInfo;
import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceWindowInfo;

public interface GetMaintenanceStatusUseCase {

    /**
     * 사용자/클라이언트에 노출할 점검 상태. 현재 활성 + 다음 예약 정보.
     */
    MaintenanceStatusInfo getStatus();

    /**
     * 어드민 콘솔용 전체 윈도우 목록. 최신순.
     */
    List<MaintenanceWindowInfo> listAll();

    /**
     * 단건 조회.
     */
    MaintenanceWindowInfo getById(Long windowId);
}
