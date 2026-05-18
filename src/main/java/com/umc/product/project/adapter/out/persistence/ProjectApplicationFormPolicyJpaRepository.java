package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationFormPolicyJpaRepository
    extends JpaRepository<ProjectApplicationFormPolicy, Long> {

    List<ProjectApplicationFormPolicy> findAllByApplicationFormId(Long applicationFormId);

    void deleteByFormSectionId(Long formSectionId);
}
