package com.umc.product.maintenance.application.port.out;

import com.umc.product.maintenance.domain.MaintenanceWindow;

public interface SaveMaintenanceWindowPort {

    MaintenanceWindow save(MaintenanceWindow window);
}
