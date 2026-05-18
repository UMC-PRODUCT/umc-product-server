package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPolicyPort;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProjectApplicationFormPolicyPersistenceAdapter
    implements LoadProjectApplicationFormPolicyPort, SaveProjectApplicationFormPolicyPort {

    private final ProjectApplicationFormPolicyJpaRepository repository;

    @Override
    public List<ProjectApplicationFormPolicy> listByApplicationFormId(Long applicationFormId) {
        return repository.findAllByApplicationFormId(applicationFormId);
    }

    @Override
    public ProjectApplicationFormPolicy save(ProjectApplicationFormPolicy policy) {
        return repository.save(policy);
    }

    @Override
    public List<ProjectApplicationFormPolicy> saveAll(List<ProjectApplicationFormPolicy> policies) {
        return repository.saveAll(policies);
    }

    @Override
    public void delete(ProjectApplicationFormPolicy policy) {
        repository.delete(policy);
    }

    @Override
    @Transactional
    public void deleteByFormSectionId(Long formSectionId) {
        repository.deleteByFormSectionId(formSectionId);
    }
}
