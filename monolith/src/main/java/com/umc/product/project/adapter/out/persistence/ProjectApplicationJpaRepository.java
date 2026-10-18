package com.umc.product.project.adapter.out.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

public interface ProjectApplicationJpaRepository extends JpaRepository<ProjectApplication, Long> {

    boolean existsByAppliedMatchingRound_Id(Long matchingRoundId);

    List<ProjectApplication> findAllByAppliedMatchingRound_Id(Long matchingRoundId);

    @Query("SELECT a FROM ProjectApplication a "
        + "WHERE a.applicationForm.project.id = :projectId "
        + "AND a.status IN :statuses")
    List<ProjectApplication> findAllByProjectIdAndStatusIn(
        @Param("projectId") Long projectId,
        @Param("statuses") List<ProjectApplicationStatus> statuses
    );

    @Query("SELECT a FROM ProjectApplication a "
        + "WHERE a.applicantMemberId = :applicantMemberId "
        + "AND a.applicationForm.project.gisuId = :gisuId "
        + "AND a.status IN :statuses")
    List<ProjectApplication> findAllByApplicantMemberIdAndGisuIdAndStatusIn(
        @Param("applicantMemberId") Long applicantMemberId,
        @Param("gisuId") Long gisuId,
        @Param("statuses") Collection<ProjectApplicationStatus> statuses
    );
}
