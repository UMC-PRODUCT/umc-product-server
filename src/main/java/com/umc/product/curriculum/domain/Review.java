package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long submissionId;

    @Column(nullable = false)
    private Long reviewerChallengerId;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewResult status;

    @Column(columnDefinition = "TEXT")
    private String bestReason;

    @Builder(access = AccessLevel.PRIVATE)
    private Review(Long submissionId, Long reviewerChallengerId, ReviewResult status, String feedback, String bestReason) {
        this.submissionId = submissionId;
        this.reviewerChallengerId = reviewerChallengerId;
        this.status = status;
        this.feedback = feedback;
        this.bestReason = bestReason;
    }

    public static Review create(Long submissionId, Long reviewerChallengerId, ReviewResult status, String feedback) {
        return Review.builder()
            .submissionId(submissionId)
            .reviewerChallengerId(reviewerChallengerId)
            .status(status)
            .feedback(feedback)
            .build();
    }

    public static Review createBest(Long submissionId, Long reviewerChallengerId, String feedback, String bestReason) {
        return Review.builder()
            .submissionId(submissionId)
            .reviewerChallengerId(reviewerChallengerId)
            .status(ReviewResult.BEST)
            .feedback(feedback)
            .bestReason(bestReason)
            .build();
    }
}
