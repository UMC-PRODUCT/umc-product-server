package com.umc.product.project.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectPartQuotaJpaRepository extends JpaRepository<ProjectPartQuota, Long> {

    List<ProjectPartQuota> findByProjectId(Long projectId);

    boolean existsByProjectIdAndPart(Long projectId, ChallengerPart part);

    @Modifying
    @Query("DELETE FROM ProjectPartQuota q "
        + "WHERE q.project.id = :projectId AND q.part IN :parts")
    void deleteByProjectIdAndPartIn(@Param("projectId") Long projectId,
                                    @Param("parts") Collection<ChallengerPart> parts);
}
