package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.MissionFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionFeedbackJpaRepository extends JpaRepository<MissionFeedback, Long> {

    List<MissionFeedback> findByMissionSubmission_IdIn(List<Long> submissionIds);
}