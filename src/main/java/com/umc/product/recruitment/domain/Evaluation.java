package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.EvaluationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "evaluation")
public class Evaluation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStage stage;

    @Column(name = "evaluator_user_id", nullable = false)
    private Long evaluatorUserId;

    @Column
    private Integer score;

    @Column
    private String comments;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStatus status = EvaluationStatus.SUBMITTED;
    // Document Evaluation에만 임시저장 로직이 있으므로, 기본값을 SUBMITTED로 설정
    // Document Evaluation 임시저장 시에만 status를 DRAFT로 생성

    // ========================================================================
    // Factory Methods
    // ========================================================================

    public static Evaluation createDocumentEvaluation(
        Application application,
        Long evaluatorUserId,
        Integer score,
        String comments,
        EvaluationStatus status
    ) {
        return Evaluation.builder()
            .application(application)
            .stage(EvaluationStage.DOCUMENT)
            .evaluatorUserId(evaluatorUserId)
            .score(score)
            .comments(comments)
            .status(status)
            .build();
    }

    public static Evaluation createInterviewEvaluation(
        Application application,
        Long evaluatorUserId,
        Integer score,
        String comments
    ) {
        return Evaluation.builder()
            .application(application)
            .stage(EvaluationStage.INTERVIEW)
            .evaluatorUserId(evaluatorUserId)
            .score(score)
            .comments(comments)
            .status(EvaluationStatus.SUBMITTED)
            .build();
    }

    // ========================================================================
    // Domain Methods
    // ========================================================================

    public void update(Integer score, String comments, EvaluationStatus status) {
        this.score = score;
        this.comments = comments;
        this.status = status;
    }

    public void updateScoreAndComments(Integer score, String comments) {
        this.score = score;
        this.comments = comments;
    }
}
