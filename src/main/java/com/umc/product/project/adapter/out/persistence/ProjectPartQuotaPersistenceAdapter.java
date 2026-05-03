package com.umc.product.project.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectPartQuotaPersistenceAdapter
    implements LoadProjectPartQuotaPort, SaveProjectPartQuotaPort {

    private final ProjectPartQuotaJpaRepository repository;

    @Override
    public List<ProjectPartQuota> listByProjectId(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    public ProjectPartQuota save(ProjectPartQuota quota) {
        return repository.save(quota);
    }

    @Override
    public List<ProjectPartQuota> saveAll(Collection<ProjectPartQuota> quotas) {
        return repository.saveAll(quotas);
    }

    @Override
    public void deleteByProjectIdAndPartIn(Long projectId, Collection<ChallengerPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return;
        }
        repository.deleteByProjectIdAndPartIn(projectId, parts);
    }
}
