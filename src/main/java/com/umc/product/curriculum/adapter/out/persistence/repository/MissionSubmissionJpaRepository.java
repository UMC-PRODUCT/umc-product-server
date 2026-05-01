package com.umc.product.curriculum.adapter.out.persistence.repository;

import com.umc.product.curriculum.domain.MissionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionSubmissionJpaRepository extends JpaRepository<MissionSubmission, Long> {

    List<MissionSubmission> findByChallengerWorkbook_IdIn(List<Long> challengerWorkbookIds);
}
