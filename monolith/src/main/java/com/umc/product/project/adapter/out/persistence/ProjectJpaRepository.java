package com.umc.product.project.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    boolean existsByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);

    Optional<Project> findByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    List<Project> findByChapterIdAndStatus(Long chapterId, ProjectStatus status);

    boolean existsByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    boolean existsByProductOwnerMemberIdAndGisuIdAndStatus(
        Long productOwnerMemberId, Long gisuId, ProjectStatus status);
}
