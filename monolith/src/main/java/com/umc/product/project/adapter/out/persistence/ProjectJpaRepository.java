package com.umc.product.project.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

import jakarta.persistence.LockModeType;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findByIdForUpdate(@Param("id") Long id);

    boolean existsByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);

    Optional<Project> findByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    List<Project> findByChapterIdAndStatus(Long chapterId, ProjectStatus status);

    boolean existsByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    boolean existsByProductOwnerMemberIdAndGisuIdAndStatus(
        Long productOwnerMemberId, Long gisuId, ProjectStatus status);
}
