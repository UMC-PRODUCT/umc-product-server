package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.curriculum.domain.MissionSubmission;

public interface MissionSubmissionJpaRepository extends JpaRepository<MissionSubmission, Long> {

    @EntityGraph(attributePaths = {"originalWorkbookMission", "challengerWorkbook"})
    List<MissionSubmission> findByChallengerWorkbook_Id(Long challengerWorkbookId);

    @EntityGraph(attributePaths = {"originalWorkbookMission", "challengerWorkbook"})
    List<MissionSubmission> findByChallengerWorkbook_IdIn(List<Long> challengerWorkbookIds);

    boolean existsByOriginalWorkbookMission_Id(Long originalWorkbookMissionId);
}
