package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectApplicationFormPersistenceAdapter
    implements LoadProjectApplicationFormPort, SaveProjectApplicationFormPort {

    private final ProjectApplicationFormJpaRepository repository;

    @Override
    public boolean existsByProjectId(Long projectId) {
        return repository.existsByProjectId(projectId);
    }

    @Override
    public Optional<ProjectApplicationForm> findByProjectId(Long projectId) {
        return repository.findFirstByProjectIdOrderByIdAsc(projectId);
    }

    @Override
    public Map<Long, ProjectApplicationForm> findAllByProjectIds(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }
        return repository.findAllByProjectIds(projectIds).stream()
            .collect(Collectors.toMap(
                form -> form.getProject().getId(),
                form -> form,
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
    }

    @Override
    public ProjectApplicationForm save(ProjectApplicationForm form) {
        return repository.save(form);
    }

    @Override
    public List<ProjectApplicationForm> saveAll(List<ProjectApplicationForm> forms) {
        return repository.saveAll(forms);
    }

    @Override
    public void delete(ProjectApplicationForm form) {
        repository.delete(form);
    }

    @Override
    public void deleteAllByProjectId(Long projectId) {
        repository.deleteAllByProjectId(projectId);
    }
}
