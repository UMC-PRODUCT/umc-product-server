package com.umc.product.maintenance.adapter.out.persistence;

import com.umc.product.maintenance.application.port.out.LoadMaintenanceWindowPort;
import com.umc.product.maintenance.application.port.out.SaveMaintenanceWindowPort;
import com.umc.product.maintenance.domain.MaintenanceWindow;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceWindowPersistenceAdapter
    implements LoadMaintenanceWindowPort, SaveMaintenanceWindowPort {

    private final MaintenanceWindowRepository repository;

    @Override
    public Optional<MaintenanceWindow> findActiveAt(Instant now) {
        return repository.findActiveAt(now).stream().findFirst();
    }

    @Override
    public Optional<MaintenanceWindow> findNextUpcoming(Instant now) {
        return repository.findUpcoming(now).stream().findFirst();
    }

    @Override
    public Optional<MaintenanceWindow> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<MaintenanceWindow> findOverlapping(Instant startAt, Instant endAt) {
        return repository.findOverlapping(startAt, endAt);
    }

    @Override
    public List<MaintenanceWindow> findAllOrderByCreatedAtDesc() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public MaintenanceWindow save(MaintenanceWindow window) {
        return repository.save(window);
    }
}
