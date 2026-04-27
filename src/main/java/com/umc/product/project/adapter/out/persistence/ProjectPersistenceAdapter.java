package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectPersistenceAdapter implements LoadProjectPort, SaveProjectPort {

    private final ProjectJpaRepository jpaRepository;
    private final ProjectQueryRepository queryRepository;

    @Override
    public Optional<Project> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Project getById(Long id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public Optional<Project> findByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        return jpaRepository.findByProductOwnerMemberIdAndGisuId(productOwnerMemberId, gisuId);
    }

    @Override
    public boolean existsByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        return jpaRepository.existsByProductOwnerMemberIdAndGisuId(productOwnerMemberId, gisuId);
    }

    @Override
    public Page<Project> search(SearchProjectQuery query) {
        return queryRepository.search(query);
    }

    @Override
    public Project save(Project project) {
        return jpaRepository.save(project);
    }

    @Override
    public List<Project> saveAll(List<Project> projects) {
        return jpaRepository.saveAll(projects);
    }

    @Override
    public void delete(Project project) {
        jpaRepository.delete(project);
    }
}
