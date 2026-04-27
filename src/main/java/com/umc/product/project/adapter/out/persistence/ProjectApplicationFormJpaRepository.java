package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationFormJpaRepository extends JpaRepository<ProjectApplicationForm, Long> {

    boolean existsByProjectId(Long projectId);
}
