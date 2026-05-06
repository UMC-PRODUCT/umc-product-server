package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationJpaRepository extends JpaRepository<ProjectApplication, Long> {

    boolean existsByAppliedMatchingRound_Id(Long matchingRoundId);
}
