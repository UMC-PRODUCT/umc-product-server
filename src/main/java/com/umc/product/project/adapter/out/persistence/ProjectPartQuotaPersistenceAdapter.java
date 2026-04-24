package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectPartQuotaPersistenceAdapter implements LoadProjectPartQuotaPort {

    private final ProjectPartQuotaJpaRepository repository;

    @Override
    public List<ProjectPartQuota> listByProjectId(Long projectId) {
        return repository.findByProjectId(projectId);
    }
}
