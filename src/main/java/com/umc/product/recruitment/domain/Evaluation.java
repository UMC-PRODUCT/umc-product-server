package com.umc.product.recruitment.domain;

import com.umc.product.recruitment.domain.enums.EvaluationDecision;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
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
public class Evaluation {

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationDecision decision = EvaluationDecision.HOLD;

    @Column
    private String memo;
}
