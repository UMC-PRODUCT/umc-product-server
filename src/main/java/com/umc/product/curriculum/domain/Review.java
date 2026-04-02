package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import org.springframework.util.StringUtils;
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
@Table(name = "challenger_workbook_submission_review")
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
    public void cancelBest() {
        if (this.status != ReviewResult.BEST) {
            throw new CurriculumDomainException(CurriculumErrorCode.REVIEW_NOT_BEST);
        }
        this.status = ReviewResult.PASS;
        this.bestReason = null;
    }

    public void upgradeToBest(String bestReason) {
        if (this.status == ReviewResult.BEST) {
            throw new CurriculumDomainException(CurriculumErrorCode.REVIEW_ALREADY_BEST);
        }
        this.status = ReviewResult.BEST;
        this.bestReason = bestReason;
    }


    public void updateBestReason(String bestReason) {
        if (this.status != ReviewResult.BEST) {
            throw new CurriculumDomainException(CurriculumErrorCode.REVIEW_NOT_BEST);
        }
        this.bestReason = bestReason;
    }

    public void updateFeedback(String feedback) {
        this.feedback = feedback;
    }

    /**
     * 리뷰 수정 (status + feedback)
     * <p>
     * BEST 상태의 리뷰는 수정할 수 없습니다. cancelBest를 먼저 호출하세요.
     */
    public void update(ReviewResult newStatus, String newFeedback) {
        if (this.status == ReviewResult.BEST) {
            throw new CurriculumDomainException(CurriculumErrorCode.REVIEW_IS_BEST);
        }
        this.status = newStatus;
        if (StringUtils.hasText(newFeedback)) {
            this.feedback = newFeedback;
        }
    }
}
