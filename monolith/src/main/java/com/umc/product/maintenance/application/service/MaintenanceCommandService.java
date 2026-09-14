package com.umc.product.maintenance.application.service;

import com.umc.product.maintenance.application.port.in.command.ManageMaintenanceUseCase;
import com.umc.product.maintenance.application.port.in.command.dto.StartMaintenanceCommand;
import com.umc.product.maintenance.application.port.out.LoadMaintenanceWindowPort;
import com.umc.product.maintenance.application.port.out.SaveMaintenanceWindowPort;
import com.umc.product.maintenance.domain.MaintenanceWindow;
import com.umc.product.maintenance.exception.MaintenanceDomainException;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaintenanceCommandService implements ManageMaintenanceUseCase {

    private final LoadMaintenanceWindowPort loadPort;
    private final SaveMaintenanceWindowPort savePort;
    private final MaintenanceStateHolder stateHolder;
    private final Clock clock;

    @Autowired
    public MaintenanceCommandService(
        LoadMaintenanceWindowPort loadPort,
        SaveMaintenanceWindowPort savePort,
        MaintenanceStateHolder stateHolder
    ) {
        this(loadPort, savePort, stateHolder, Clock.systemUTC());
    }

    MaintenanceCommandService(
        LoadMaintenanceWindowPort loadPort,
        SaveMaintenanceWindowPort savePort,
        MaintenanceStateHolder stateHolder,
        Clock clock
    ) {
        this.loadPort = loadPort;
        this.savePort = savePort;
        this.stateHolder = stateHolder;
        this.clock = clock;
    }

    @Override
    public Long start(StartMaintenanceCommand command) {
        var now = clock.instant();
        List<MaintenanceWindow> overlapping = loadPort.findOverlapping(command.startAt(), command.endAt());
        if (!overlapping.isEmpty()) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.OVERLAPPING_WINDOW);
        }
        MaintenanceWindow window = MaintenanceWindow.of(
            command.scope(),
            command.targetDomains(),
            command.startAt(),
            command.endAt(),
            command.title(),
            command.message(),
            command.createdBy(),
            now
        );
        MaintenanceWindow saved = savePort.save(window);
        stateHolder.refresh();
        return saved.getId();
    }

    @Override
    public void forceEnd(Long windowId, Long requestedBy) {
        MaintenanceWindow window = loadPort.findById(windowId)
            .orElseThrow(() -> new MaintenanceDomainException(MaintenanceErrorCode.MAINTENANCE_WINDOW_NOT_FOUND));
        window.forceEnd(clock.instant(), requestedBy);
        savePort.save(window);
        stateHolder.refresh();
    }
}
