package com.umc.product.maintenance.application.service;

import com.umc.product.maintenance.application.port.in.query.GetMaintenanceStatusUseCase;
import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceStatusInfo;
import com.umc.product.maintenance.application.port.in.query.dto.MaintenanceWindowInfo;
import com.umc.product.maintenance.application.port.out.LoadMaintenanceWindowPort;
import com.umc.product.maintenance.exception.MaintenanceDomainException;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceQueryService implements GetMaintenanceStatusUseCase {

    private final LoadMaintenanceWindowPort loadPort;
    private final Clock clock;

    @Autowired
    public MaintenanceQueryService(LoadMaintenanceWindowPort loadPort) {
        this(loadPort, Clock.systemUTC());
    }

    MaintenanceQueryService(LoadMaintenanceWindowPort loadPort, Clock clock) {
        this.loadPort = loadPort;
        this.clock = clock;
    }

    @Override
    public MaintenanceStatusInfo getStatus() {
        var now = clock.instant();
        MaintenanceWindowInfo current = loadPort.findActiveAt(now)
            .map(MaintenanceWindowInfo::from)
            .orElse(null);
        MaintenanceWindowInfo upcoming = loadPort.findNextUpcoming(now)
            .map(MaintenanceWindowInfo::from)
            .orElse(null);
        return new MaintenanceStatusInfo(current != null, current, upcoming);
    }

    @Override
    public List<MaintenanceWindowInfo> listAll() {
        return loadPort.findAllOrderByCreatedAtDesc().stream()
            .map(MaintenanceWindowInfo::from)
            .toList();
    }

    @Override
    public MaintenanceWindowInfo getById(Long windowId) {
        return loadPort.findById(windowId)
            .map(MaintenanceWindowInfo::from)
            .orElseThrow(() -> new MaintenanceDomainException(MaintenanceErrorCode.MAINTENANCE_WINDOW_NOT_FOUND));
    }
}
