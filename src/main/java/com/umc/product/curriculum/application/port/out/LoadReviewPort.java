package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Review;
import java.util.List;

public interface LoadReviewPort {

    List<Review> findAllBySubmissionId(Long submissionId);

    boolean existsBySubmissionIdAndReviewerChallengerId(Long submissionId, Long reviewerChallengerId);
}
