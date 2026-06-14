package com.umc.product.project.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

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
    public List<Project> listByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<Project> listByChapterIdAndStatus(Long chapterId, ProjectStatus status) {
        return jpaRepository.findByChapterIdAndStatus(chapterId, status);
    }

    @Override
    public boolean existsByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        return jpaRepository.existsByProductOwnerMemberIdAndGisuId(productOwnerMemberId, gisuId);
    }

    @Override
    public Optional<Project> findDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId) {
        return jpaRepository.findByCreatorMemberIdAndGisuIdAndStatus(
            creatorMemberId, gisuId, ProjectStatus.DRAFT);
    }

    @Override
    public boolean existsDraftByCreatorAndGisu(Long creatorMemberId, Long gisuId) {
        return jpaRepository.existsByCreatorMemberIdAndGisuIdAndStatus(
            creatorMemberId, gisuId, ProjectStatus.DRAFT);
    }

    @Override
    public boolean existsDraftByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        return jpaRepository.existsByProductOwnerMemberIdAndGisuIdAndStatus(
            productOwnerMemberId, gisuId, ProjectStatus.DRAFT);
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
