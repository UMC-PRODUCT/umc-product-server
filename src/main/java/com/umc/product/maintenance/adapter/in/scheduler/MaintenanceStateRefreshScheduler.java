package com.umc.product.maintenance.adapter.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.maintenance.application.service.MaintenanceStateHolder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MaintenanceStateRefreshScheduler {

    private final MaintenanceStateHolder stateHolder;

    @Scheduled(fixedDelay = 10_000L)
    public void refresh() {
        stateHolder.refresh();
    }
}
