package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadReviewPort;
import com.umc.product.curriculum.application.port.out.SaveReviewPort;
import com.umc.product.curriculum.domain.Review;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewPersistenceAdapter implements LoadReviewPort, SaveReviewPort {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public List<Review> findAllBySubmissionId(Long submissionId) {
        return reviewJpaRepository.findAllBySubmissionId(submissionId);
    }

    @Override
    public List<Review> findAllBySubmissionIdAndStatus(Long submissionId, ReviewResult status) {
        return reviewJpaRepository.findAllBySubmissionIdAndStatus(submissionId, status);
    }

    @Override
    public boolean existsBySubmissionIdAndReviewerChallengerId(Long submissionId, Long reviewerChallengerId) {
        return reviewJpaRepository.existsBySubmissionIdAndReviewerChallengerId(submissionId, reviewerChallengerId);
    }

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }
}
