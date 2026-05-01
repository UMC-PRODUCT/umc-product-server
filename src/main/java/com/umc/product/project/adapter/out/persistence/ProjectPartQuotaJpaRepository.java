package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPartQuotaJpaRepository extends JpaRepository<ProjectPartQuota, Long> {

    List<ProjectPartQuota> findByProjectId(Long projectId);
}
