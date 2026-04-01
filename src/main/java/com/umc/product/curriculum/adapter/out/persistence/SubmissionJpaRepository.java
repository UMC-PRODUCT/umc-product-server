package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.Submission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionJpaRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByChallengerWorkbookId(Long challengerWorkbookId);
}
