package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectApplicationFormJpaRepository extends JpaRepository<ProjectApplicationForm, Long> {

    boolean existsByProjectId(Long projectId);

    Optional<ProjectApplicationForm> findFirstByProjectIdOrderByIdAsc(Long projectId);

    @Query("""
        SELECT f
        FROM ProjectApplicationForm f
        JOIN FETCH f.project p
        WHERE p.id IN :projectIds
        ORDER BY f.id ASC
        """)
    List<ProjectApplicationForm> findAllByProjectIds(@Param("projectIds") Collection<Long> projectIds);

    @Modifying
    @Query("DELETE FROM ProjectApplicationForm f WHERE f.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}
