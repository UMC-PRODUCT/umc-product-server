package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.MissionSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionSubmissionJpaRepository extends JpaRepository<MissionSubmission, Long> {

    List<MissionSubmission> findByChallengerWorkbook_IdIn(List<Long> challengerWorkbookIds);
}