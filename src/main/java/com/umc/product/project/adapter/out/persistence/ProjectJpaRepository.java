package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    boolean existsByProductOwnerMemberIdAndGisuId(Long productOwnerMemberId, Long gisuId);

    boolean existsByProductOwnerMemberIdAndGisuIdAndChapterId(Long productOwnerMemberId, Long gisuId, Long chapterId);

    Optional<Project> findByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);

    boolean existsByCreatorMemberIdAndGisuIdAndStatus(
        Long creatorMemberId, Long gisuId, ProjectStatus status);
}
