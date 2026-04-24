package com.umc.product.project.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberJpaRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectIdAndPartAndStatus(Long projectId, ChallengerPart part, ProjectMemberStatus status);

    @Query("SELECT pm.part, COUNT(pm) FROM ProjectMember pm "
        + "WHERE pm.project.id = :projectId AND pm.status = :status "
        + "GROUP BY pm.part")
    List<Object[]> countByProjectIdGroupByPartRaw(@Param("projectId") Long projectId, @Param("status") ProjectMemberStatus status);
}
