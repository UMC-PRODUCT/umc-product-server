package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.Review;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findAllBySubmissionId(Long submissionId);

    List<Review> findAllBySubmissionIdAndStatus(Long submissionId, ReviewResult status);

    boolean existsBySubmissionIdAndReviewerChallengerId(Long submissionId, Long reviewerChallengerId);
}
