package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectApplicationFormPersistenceAdapter implements LoadProjectApplicationFormPort {

    private final ProjectApplicationFormJpaRepository repository;

    @Override
    public boolean existsByProjectId(Long projectId) {
        return repository.existsByProjectId(projectId);
    }
}
