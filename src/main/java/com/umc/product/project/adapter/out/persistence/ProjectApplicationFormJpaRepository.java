package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectApplicationFormJpaRepository extends JpaRepository<ProjectApplicationForm, Long> {

    boolean existsByProjectId(Long projectId);

    Optional<ProjectApplicationForm> findFirstByProjectIdOrderByIdAsc(Long projectId);

    @Modifying
    @Query("DELETE FROM ProjectApplicationForm f WHERE f.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}
