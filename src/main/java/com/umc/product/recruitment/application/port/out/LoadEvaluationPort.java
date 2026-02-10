package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadEvaluationPort {

    Optional<Evaluation> findByApplicationIdAndEvaluatorUserIdAndStage(
        Long applicationId,
        Long evaluatorUserId,
        EvaluationStage stage
    );

    List<Evaluation> findByApplicationIdAndStage(Long applicationId, EvaluationStage evaluationStage);

    /**
     * 주어진 applicationIds 중 특정 evaluator가 특정 stage에서 평가한 applicationId 목록 반환
     */
    Set<Long> findApplicationIdsWithEvaluations(
        Set<Long> applicationIds,
        Long evaluatorUserId,
        EvaluationStage stage
    );
}
