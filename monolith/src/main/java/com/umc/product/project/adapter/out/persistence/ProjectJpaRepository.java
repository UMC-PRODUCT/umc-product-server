package com.umc.product.project.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    boolean existsByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);

    Optional<Project> findFirstByCreatorMemberIdAndGisuIdAndStatusOrderByCreatedAtDescIdDesc(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    Page<Project> findAllByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status, Pageable pageable);

    List<Project> findByChapterIdAndStatus(Long chapterId, ProjectStatus status);

}
