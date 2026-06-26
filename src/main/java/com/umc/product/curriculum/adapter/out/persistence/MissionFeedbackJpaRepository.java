package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.curriculum.domain.MissionFeedback;

public interface MissionFeedbackJpaRepository extends JpaRepository<MissionFeedback, Long> {

    @EntityGraph(attributePaths = {"missionSubmission"})
    List<MissionFeedback> findByMissionSubmission_IdIn(List<Long> submissionIds);
}
