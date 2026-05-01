package com.umc.product.curriculum.adapter.out.persistence.repository;

import com.umc.product.curriculum.domain.MissionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionFeedbackJpaRepository extends JpaRepository<MissionFeedback, Long> {

    List<MissionFeedback> findByMissionSubmission_IdIn(List<Long> submissionIds);
}
