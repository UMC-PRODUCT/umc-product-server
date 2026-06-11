package com.umc.product.project.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// TODO: 프로젝트 전반의 QueryDSL 일관성을 위해 ProjectMemberQueryRepository로 마이그레이션 필요.
public interface ProjectMemberJpaRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectIdAndPartAndStatusOrderByCreatedAtAscIdAsc(
        Long projectId, ChallengerPart part, ProjectMemberStatus status
    );

    List<ProjectMember> findByProjectIdAndStatusOrderByCreatedAtAscIdAsc(Long projectId, ProjectMemberStatus status);

    List<ProjectMember> findByProjectIdInAndStatusOrderByCreatedAtAscIdAsc(
        Collection<Long> projectIds, ProjectMemberStatus status
    );

    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    @Query("SELECT pm.part, COUNT(pm) FROM ProjectMember pm "
        + "WHERE pm.project.id = :projectId AND pm.status = :status "
        + "GROUP BY pm.part")
    List<Object[]> countByProjectIdGroupByPartRaw(@Param("projectId") Long projectId, @Param("status") ProjectMemberStatus status);

    boolean existsByProject_GisuIdAndMemberIdAndStatus(Long gisuId, Long memberId, ProjectMemberStatus status);

    boolean existsByProjectIdAndMemberIdAndPartAndStatus(
        Long projectId, Long memberId, ChallengerPart part, ProjectMemberStatus status
    );

    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}
