package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplication;

public interface SaveProjectApplicationPort {
    ProjectApplication save(ProjectApplication application);
}
