package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Review;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import java.util.List;

public interface LoadReviewPort {

    List<Review> findAllBySubmissionId(Long submissionId);

    List<Review> findAllBySubmissionIdAndStatus(Long submissionId, ReviewResult status);

    boolean existsBySubmissionIdAndReviewerChallengerId(Long submissionId, Long reviewerChallengerId);

    Review getById(Long reviewId);
}
